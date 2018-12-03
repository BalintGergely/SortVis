package g.sort.vis;

import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;
/**
 * A utility class used to distribute recursive tasks between multiple threads.
 * @author balintgergely
 *
 */
public class Multitasker {
	@SuppressWarnings("unchecked")
	private static <E extends Throwable> void throwEx(Throwable t) throws E{
		throw (E)t;
	}
	/**
	 * The NU__ task is used to wake up workers and prompt them to check the number of workers.
	 */
	private Task THE_NULL_TASK = new Task(null);
	/**
	 * The factory for threads
	 */
	private ThreadFactory factory;
	/**
	 * The phaser to ensure that the same task takes the same amount of time.
	 */
	private volatile Phaser phaser;
	/**
	 * The task queue.
	 */
	private LinkedTransferQueue<Task> queue = new LinkedTransferQueue<>();
	/**
	 * This map is used to keep track of all threads.
	 */
	private IdentityHashMap<Thread,Object> threads = new IdentityHashMap<>();
	/**
	 * The number of threads to use
	 */
	private volatile int threadLimit = 1;
	/**
	 * Used to give a unique identifier for all threads.
	 */
	private AtomicInteger threadCounter = new AtomicInteger();
	/**
	 * The main thread
	 */
	private Thread main;
	/**
	 * The phaser factory for custom phasers.
	 */
	private Supplier<Phaser> phaseSupplier;
	public Multitasker(ThreadFactory threadFactory,Supplier<Phaser> phaserFactory,Runnable mainLoop){
		factory = threadFactory;
		phaseSupplier = phaserFactory;
		phaser = phaseSupplier.get();
		main = factory.newThread(() -> {
			boolean ex = true;
			try{
				mainLoop.run();
				ex = false;
			}finally{
				if(ex){
					synchronized(threads){
						threads.remove(Thread.currentThread());
						if(threads.size() < threadLimit){
							addThread();
						}
					}
				}
			}
			work();
		});
		main.setName("MT-Thread-"+threadCounter.getAndIncrement());
		threads.put(main, null);
		main.start();
	}
	private void work(){
		Thread th = Thread.currentThread();
		System.out.println(th.getName()+" WORK");
		while(true){//Loops this thread while needed
			if(threads.size() > threadLimit){
				synchronized(threads){
					if(threads.size() > threadLimit){
						System.out.println(th.getName()+" EXIT");
						threads.remove(th);//Thread no longer needed. Quit.
						return;
					}
				}
			}
			try{//While we have tasks to run, we are registered to the phaser. If we no longer have any task, we deregister ourselves.
				Task rn = queue.take();
				if(rn != null && rn != THE_NULL_TASK) {
					System.out.println(th.getName()+" RUN TASK");
					try{
						rn.run(false);
						while((rn = queue.poll()) != null){
							if(rn == THE_NULL_TASK){
								break;
							}
							System.out.println(th.getName()+" RUN MORE TASKS");
							rn.run(true);
						}
					}finally{
						System.out.println(th.getName()+" STOPPED RUNNING TASKS");
						phaser.arriveAndDeregister();
					}
				}
			}catch(Throwable t) {}
		}
	}
	private void addThread(){
		assert Thread.holdsLock(threads);
		Thread t = factory.newThread(this::work);
		t.setName("MT-Thread-"+threadCounter.getAndIncrement());
		threads.put(t, null);
		t.start();
	}
	/**
	 * Finishes the phase. Returns the phase value. Throws if the phaser was terminated.
	 */
	public int phase(){
		int i = phaser.arriveAndAwaitAdvance();
		if(i < 0){
			throw new RuntimeException();
		}
		return i;
	}
	/**
	 * Interrupts all threads and terminates the phaser.
	 */
	public void purge(){
		synchronized(threads){
			phaser.forceTermination();
			queue.clear();
			for(Thread t : threads.keySet()){
				t.interrupt();
			}
		}
	}
	/**
	 * Resets the phaser.
	 */
	public void reset(){
		synchronized(threads){
			if(phaser.isTerminated()){
				phaser = phaseSupplier.get();
			}
		}
	}
	/**
	 * Sets the thread count to the specified value. Also ensures that there are at least that many threads running.
	 */
	public void setThreadCount(int limit){
		if(limit <= 0){
			throw new IllegalArgumentException();
		}
		int delta;
		synchronized(threads){
			threadLimit = limit;
			delta = threads.size()-limit;
			while(threads.size() < threadLimit){
				addThread();
			}
		}
		while(delta > 0){
			queue.add(THE_NULL_TASK);
			delta--;
		}
	}
	private Task newTaskFor(Runnable r){
		Task t = new Task(r);
		if(queue.tryTransfer(t)){
			phaser.register();
			t.spin = false;
		}else{
			t.spin = false;
			t.registered = false;
			queue.add(t);
		}
		return t;
	}
	/**
	 * Runs both tasks. This method blocks until both are finished either exceptionally or not.
	 */
	public void run2(Runnable a,Runnable b){
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);
		Task taskb = newTaskFor(b);
		try{
			a.run();
		}finally{
			taskb.join();
		}
	}
	/**
	 * Runs all three tasks. This method blocks until all are finished either exceptionally or not.
	 */
	public void run3(Runnable a,Runnable b,Runnable c){
		Objects.requireNonNull(a);
		Objects.requireNonNull(b);
		Objects.requireNonNull(c);
		Task taskb = newTaskFor(b);
		Task taskc = newTaskFor(c);
		try{
			a.run();
		}finally{
			try{
				taskb.join();
			}finally{
				taskc.join();
			}
		}
	}
	private class Task extends AtomicReference<Thread>{
		private static final long serialVersionUID = 1L;
		private volatile Object r;//The runnable and th throwable result if there is one
		private volatile boolean registered = true;//Indicates if the client thread registered the calling thread
		/**
		 * Depending on whether this task was transferred or queued in the task queue, this value indicates if the external initialization is not done yet.
		 */
		private volatile boolean spin = true;
		public Task(Runnable r){
			this.r = r;
		}
		public void run(boolean reg) {//Indicates if the calling thread is already registered
			while(spin){
				Thread.onSpinWait();//This can only happen in very rare cases I believe. It depends on the inner workings of the queue.
			}
			if(registered == reg){//Ensure that this thread is registered exactly once.
				if(reg){//Two registers for this thread. Deregister one.
					//This can only happen if this task object went through queue.tryTransfer(Task) -> queue.poll()
					//Since both methods return immediately, I don't know if this code is even reachable.
					phaser.arriveAndDeregister();
				}else{//No registers for this thread. Register one
					phaser.register();
				}
			}
			//At this point there is exactly ONE register for the calling thread
			Thread thread = Thread.currentThread();
			if(compareAndSet(null,thread)){
				try{
					((Runnable)r).run();
					r = null;
				}catch(Throwable t){
					r = t;
				}
				Thread oth = getAndSet(null);
				if(oth != null){
					LockSupport.unpark(oth);
				}
			}
		}
		/**
		 * Joins with this task. If it was not started, this method will run it. If it was started, this method will block until it finishes.
		 */
		public void join(){
			Thread thread = Thread.currentThread();
			Thread alt = getAndSet(thread);
			if(alt != null){
				phaser.arriveAndDeregister();
				try{
					while(get() == thread){
						if(thread.isInterrupted()){
							alt.interrupt();//XXX Does this method spin? Can LockSupport.park() block if called multiple times while the thread is interrupted? Mystery.
						}
						LockSupport.park(this);//We use park/unpark here because we have to block until the task finishes.
					}
				}finally{
					phaser.register();
				}
			}else if(r instanceof Runnable){
				((Runnable)r).run();//In the case this task wasn't even started, we run it.
			}
			if(r instanceof Throwable){
				throwEx(new ExecutionException((Throwable)r));
			}
		}
	}
}
