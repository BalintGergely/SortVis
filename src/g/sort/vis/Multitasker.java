package g.sort.vis;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.IdentityHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
/**
 * A utility class used to distribute recursive tasks between multiple threads.
 * @author balintgergely
 *
 */
public class Multitasker implements Executor{
	private static final VarHandle TASK;
	static {
		try {
			Lookup lk = MethodHandles.lookup();
			TASK = lk.findVarHandle(Multitasker.class, "mainTask", Runnable.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
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
	private LinkedTransferQueue<Runnable> queue = new LinkedTransferQueue<>();
	/**
	 * This map is used to keep track of all threads.
	 */
	private IdentityHashMap<Thread,Object> threads = new IdentityHashMap<>();
	/**
	 * The defaultTask is run if there are no tasks received for this much time.
	 */
	private long blockTime;
	private Runnable mainTask;
	/**
	 * The number of threads to use
	 */
	private volatile int threadLimit = 1;
	/**
	 * Used to give a unique identifier for all threads.
	 */
	private AtomicInteger threadCounter = new AtomicInteger();
	private AtomicInteger taskCounter = new AtomicInteger();
	private DisplayInterface didf;
	/**
	 * The main thread
	 */
	private Thread main;
	/**
	 * The phaser factory for custom phasers.
	 */
	private Supplier<Phaser> phaseSupplier;
	public Multitasker(ThreadFactory threadFactory,Supplier<Phaser> phaserFactory,DisplayInterface didf,long blockTime){
		factory = threadFactory;
		phaseSupplier = phaserFactory;
		phaser = phaseSupplier.get();
		main = factory.newThread(this::work);
		this.didf = didf;
		main.setName("MT-Thread-"+threadCounter.getAndIncrement());
		threads.put(main, null);
		main.start();
	}
	private void work(){
		Thread th = Thread.currentThread();
		System.out.println(th.getName()+" WORK");
		boolean taskFlag = false;
		while(true){//Loops this thread while needed
			if(th == main){
				if(taskCounter.get() <= 0){
					taskCounter.set(0);
					if(taskFlag){
						didf.running(false);
						taskFlag = false;
					}
					try{
						didf.tick();
						didf.notesOff();
						Runnable rn = mainTask;
						if(rn != null){
							reset();
							taskCounter.incrementAndGet();
							try{
								phaser.register();
								didf.running(true);
								taskFlag = true;
								rn.run();
							}catch(Throwable t){
								t.printStackTrace(System.out);
							}finally{
								if(TASK.compareAndSet(this,rn,(Runnable)null)){
									
								}
								phaser.arriveAndDeregister();
								taskCounter.decrementAndGet();
							}
						}
					}catch(Throwable t){
						t.printStackTrace(System.out);
					}
				}
			}else if(threads.size() > threadLimit){
				synchronized(threads){
					if(threads.size() > threadLimit){
						System.out.println(th.getName()+" EXIT");
						threads.remove(th);//Thread no longer needed. Quit.
						return;
					}
				}
			}
			try{//While we have tasks to run, we are registered to the phaser. If we no longer have any tasks, we deregister ourselves.
				Runnable rn = queue.poll(blockTime,TimeUnit.MILLISECONDS);
				if(rn != null) {
					System.out.println(th.getName()+" RUN TASK");
					phaser.register();
					try{
						rn.run();
						while((rn = queue.poll()) != null){
							System.out.println(th.getName()+" RUN MORE TASKS");
							try{
								rn.run();
							}finally{
								taskCounter.decrementAndGet();
							}
						}
					}finally{
						if(taskCounter.decrementAndGet() == 0){
							main.interrupt();
						}
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
	public void setMainTask(Runnable rn){
		if(mainTask != rn){
			if(taskCounter.get() > 0){
				purge();
			}
			mainTask = rn;
		}
	}
	public boolean isTaskRunning(){
		return taskCounter.get() > 0;
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
			while(queue.poll() != null){
				taskCounter.decrementAndGet();
			}
			for(Thread t : threads.keySet()){
				t.interrupt();
			}
			while(queue.poll() != null){
				taskCounter.decrementAndGet();
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
			delta--;
		}
	}
	public void execute(Runnable rn){
		taskCounter.incrementAndGet();
		queue.add(rn);
	}
}
