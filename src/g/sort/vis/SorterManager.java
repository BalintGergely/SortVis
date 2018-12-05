package g.sort.vis;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
/**
 * Responsible for handling the Array, the effects and the Multitasker.
 */
public class SorterManager implements Sorter{
	private static final VarHandle TASK;
	static {
		try {
			Lookup lk = MethodHandles.lookup();
			TASK = lk.findVarHandle(SorterManager.class, "task", Runnable.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	private class CustomPhaser extends Phaser{
		private volatile Thread advancingThread;
		private long nextWait;
		/**
		 * This custom onAdvance implementation is used to block progress for each operation.
		 */
		public boolean onAdvance(int phase, int registeredParties) {
			long dt = delayTime,mpt = TimeUnit.MILLISECONDS.toNanos(200);
			try{
				if(timedPermit){
					try{
						if(dt > 0){//Block for a certain amount of time
							dt = TimeUnit.MILLISECONDS.toNanos(dt);
							do{
								if(Thread.interrupted()){
									Thread.currentThread().interrupt();
									break;
								}
								didf.tick();
								LockSupport.parkNanos(Math.min(nextWait-System.nanoTime(), mpt));
							}while(System.nanoTime() < nextWait);
						}else if(dt <= 0){//Call didf.tick() once per a specified number of frames
							if(dt == 0 || (phase % (-dt)) == 0){
								didf.tick();
							}
						}
					}finally{
						didf.notesOff();
					}
				}else{//We do not have the timed permit, so we block until woken up.
					advancingThread = Thread.currentThread();
					didf.tick();
					while(advancingThread != null){
						if(Thread.interrupted()){
							Thread.currentThread().interrupt();
							break;
						}
						LockSupport.parkNanos(mpt);
						didf.notesOff();
						didf.tick();
					}
				}
			}catch(Throwable e){
				if(e instanceof InterruptedException || e.getCause() instanceof InterruptedException){
					Thread.currentThread().interrupt();
				}else{
					e.printStackTrace();
				}
			}finally{
				if(dt > 0){
					nextWait = System.nanoTime()+dt;
				}
			}
			return false;
		}
		/**
		 * Unblocks the thread waiting in onAdvance. This is the mechanism behind the step button.
		 */
		public void notifyThread(){
			Thread t = advancingThread;
			advancingThread = null;
			LockSupport.unpark(t);
		}
	}
	private Multitasker multitasker;
	private VisualArray array;
	/**
	 * <li>If positive, the minimum delay to wait between every frame
	 * <li>If negative, the amount of frames to perform between draws on the screen
	 */
	private volatile int delayTime;
	/**
	 * If false, the user stopped the procedure and we have to wait until either interrupted or stepped.
	 */
	private boolean timedPermit;
	private CustomPhaser phaser;
	private Random random = new Random();
	private DisplayInterface didf;
	private Runnable task;//WarHandle'd
	
	private BiIntConsumer ownEvent = (int a,int b) -> {
		int dt = delayTime;
		int phase = multitasker.phase();
		if(dt >= 0 || (phase % (-dt)) == 0){
			a = a < 0 ? -1 : (a-array.min)*127/array.range;
			b = b < 0 ? -1 : (b-array.min)*127/array.range;
			if(a >= 0 && a < 128)didf.noteOn(a);
			if(b >= 0 && b < 128)didf.noteOn(b);
		}
	};
	public final Node<Iterable<ConfigurableSorter>> root;
	private void run(){
		while(true){
			try{
				didf.tick();//We still have to tick because cooldowns always happen
				Runnable tsk = task;
				if(tsk == null){
					LockSupport.parkNanos(200000000);
				}else{
					didf.running(true);
					phaser.register();
					try{
						tsk.run();//When this method returns, no internal thread should run any task. At all.
					}finally{
						phaser.arriveAndDeregister();
						if(!TASK.compareAndSet(this,tsk,null)){
							System.out.println("Task updated while working.");
							//XXX Do not remove the debug here. Without this, the task field won't be set at all due to a Java bug. Try something else like LockSupport.parkNanos()
						}
					}
					didf.running(false);
				}
				didf.notesOff();
			}catch(Throwable e){
				if(!(e instanceof InterruptedException || e.getCause() instanceof InterruptedException)){
					e.printStackTrace(System.out);
				}
			}
			multitasker.reset();
		}
	}
	private IdentityHashMap<Object,Sorter> sorterMap;
	public SorterManager(DisplayInterface didf) {
		sorterMap = new IdentityHashMap<>();
		this.didf = Objects.requireNonNull(didf);
		final ServiceLoader<ConfigurableSorter> ldr = ServiceLoader.load(ConfigurableSorter.class);
		int count = 0;
		try{
			Iterator<ConfigurableSorter> itr = ldr.iterator();
			while(itr.hasNext()){
				itr.next();
				count++;
			}
		}catch(ServiceConfigurationError e){
			e.printStackTrace();
			count = 0;
		}
		root = new Node<>(count == 0 ? new EmergencyLoader() : new Iterable<ConfigurableSorter>(){
			public Iterator<ConfigurableSorter> iterator(){
				return ldr.iterator();
			}
			public String toString(){
				return "Random";
			}
		});
		fill(root);
		multitasker = new Multitasker(Executors.privilegedThreadFactory(),
				() -> phaser = new CustomPhaser(),
				this::run);
	}
	@SuppressWarnings("unchecked")
	private void fill(Node<? extends Iterable<? extends Sorter>> str){
		for(Sorter s : str.element){
			Node<? extends Sorter> node = str.createChild(s);
			sorterMap.put(node, s);
			if(node.element instanceof Iterable){
				fill((Node<? extends Iterable<? extends Sorter>>)node);
			}
		}
	}
	public VisualArray getArray(){
		return array;
	}
	public int getDelayTime() {
		return delayTime;
	}
	public void setDelayTime(int delayTime) {
		this.delayTime = delayTime;
	}
	private Sorter getSorter(){
		Sorter s = sorterMap.get(didf.getSelectedNode());
		if(s == null){
			int ix = ThreadLocalRandom.current().nextInt(sorterMap.size());
			for(Sorter so : sorterMap.values()){
				ix--;
				if(ix < 0){
					return so;
				}
			}
		}
		return s;
	}
	@Override
	public void sort(VisualArray vis, Sorter srt) {
		if(Thread.interrupted()){
			throw new RuntimeException(new InterruptedException());
		}
		switch(vis.size){
		case 3:vis.compareAndSwap(0, 1);
				if(vis.compareAndSwap(1, 2) < 0){
					break;
				}
		case 2:vis.compareAndSwap(0, 1);
		case 1:
		case 0:break;
		default:
			Sorter s = getSorter();
			s.sort(vis, this);
			if(!vis.localCheck()){
				throw new IllegalStateException(s.getClass().getName()+" DEFECTED AT ARRAY SIZE "+vis.size);
			}
		}
	}
	public void sort(VisualArray vis0, VisualArray vis1, Sorter srt){
		if(vis0.overlaps(vis1)){
			throw new IllegalArgumentException();
		}
		if(vis0.size < vis1.size){
			multitasker.run2(
					() -> sort(vis1,srt),
					() -> sort(vis0,srt));
		}else{
			multitasker.run2(
					() -> sort(vis0,srt),
					() -> sort(vis1,srt));
		}
	}
	public void sort(VisualArray a, VisualArray b, VisualArray c, Sorter srt){
		if(a.overlaps(b) || b.overlaps(c) || c.overlaps(a)){
			throw new IllegalArgumentException();
		}
		final VisualArray vis0,vis1,vis2;
		if(a.size > b.size){//			0aa	bb0	ccc
			if(a.size > c.size){// 		00a	bb0	cc0 els	0a0 b00 00c
				vis0 = a;
				if(b.size > c.size){//	00a	0b0	c00 els 00a	b00	0c0
					vis1 = b;
					vis2 = c;
				}else{
					vis1 = c;
					vis2 = b;
				}
			}else{
				vis0 = c;
				vis1 = a;
				vis2 = b;
			}
		}else{//						aa0 0bb ccc
			if(a.size < c.size){//		a00 0bb 0cc els 0a0 00b c00
				vis2 = a;
				if(b.size < c.size){//	a00 0b0 00c els a00 00b 0c0
					vis1 = b;
					vis0 = c;
				}else{
					vis1 = c;
					vis0 = b;
				}
			}else{
				vis0 = b;
				vis1 = a;
				vis2 = c;
			}
		}
		multitasker.run3(
				() -> sort(vis0,srt),
				() -> sort(vis1,srt),
				() -> sort(vis2,srt));
	}
	public void stop(){
		multitasker.purge();
	}
	private void setTask(Runnable rn){
		if(task != rn){
			task = rn;
			multitasker.purge();
		}
	}
	public void setThreadCount(int count){
		multitasker.setThreadCount(count);
	}
	public boolean isTaskRunning(){
		Runnable t = task;
		return t != null;
	}
	public void recreate(ValueSetGenerator values,int size){
		setTask(() -> {array = new VisualArray((int i) -> values.apply(i, size, random),size,ownEvent);didf.arrayChanged();});
	}
	public void stepLock(){
		timedPermit = false;
	}
	public boolean isStepLock(){
		return !timedPermit;
	}
	public void stepDo(){
		if(phaser != null){
			phaser.notifyThread();
		}
	}
	public void stepUnlock(){
		timedPermit = true;
		stepDo();
	}
	public void shuffle(boolean notify){
		if(array != null){
			setTask(() -> {array.shuffle(random, notify);});
		}
	}
	public void reset(){
		if(array != null){
			setTask(() -> {array.cheatSort();});
		}
	}
	public void check(){
		if(array != null){
			setTask(() -> {array.check();});
		}
	}
	private Runnable TASK_SORT = () -> {
		VisualArray s = array;
		if(s != null){
			sort(s, null);
			s.check();
		}
	};
	public void doSort(){
		if(array != null){
			setTask(TASK_SORT);
		}
	}
}
