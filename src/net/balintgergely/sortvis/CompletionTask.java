package net.balintgergely.sortvis;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.VarHandle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
/**
 * A custom implementation of CompletionStage that sacrifices the ability to link a stage more than once in favor of speed and simplicity.
 * It has a significantly better performance than CompletableFuture.<br><br>
 * 
 * Every instance of this class has a "treshold". It's run or accept methods have to be called that many times before the stage is completed.
 * Each stage supports adding a single runnable that will be run as soon as it completes. So it is possible to make a stage dependent on <code>n</code> other stages
 * by giving it a treshold of <code>n</code> and registering it to be run after every other stage.<br><br>
 * 
 * Async methods without an Executor parameter throw an <code>UnsupportedOperationException</code> because CompletionTask does not have a default executor.
 * <code>ForkJoinPool.commonPool()</code> could be used but right now, Sorters are not supposed to rely on anything but the executor supplied to them.
 * 
 * @author balintgergely
 *
 * @param <E> The return type of this completion task.
 */
public class CompletionTask<E> implements Runnable,CompletionStage<E>,Consumer<Object>{
	public static final CompletionTask<?> COMPLETED_TASK = new CompletionTask<>();
	public static final UnaryOperator<?> SELF_RETURN = (Object o) -> o;
	public static CompletionStage<?> combine(CompletionStage<?> a,CompletionStage<?> b,Runnable rn){
		return toTask(a).x(toTask(b).x(new CompletionTask<>(rn, 2)));
	}
	public static CompletionStage<?> combine(CompletionStage<?> a,CompletionStage<?> b,CompletionStage<?> c,Runnable rn){
		return toTask(a).x(toTask(b).x(toTask(c).x(new CompletionTask<>(rn, 3))));
	}
	public static CompletionStage<?> combine(CompletionStage<?> a,CompletionStage<?> b,CompletionStage<?> c,CompletionStage<?> d,Runnable rn){
		return toTask(a).x(toTask(b).x(toTask(c).x(toTask(d).x(new CompletionTask<>(rn, 4)))));
	}
	public static CompletionStage<?> combine(CompletionStage<?>[] a,Runnable rn){
		if(a.length == 0){
			rn.run();
			return COMPLETED_TASK;
		}
		if(a.length == 1){
			return toTask(a[0]).x(new CompletionTask<>(rn, 1));
		}
		CompletionTask<?> all = new CompletionTask<>(rn, a.length);
		for(CompletionStage<?> s : a){
			toTask(s).x(all);
		}
		return all;
	}
	@SuppressWarnings("unchecked")
	public static <E> CompletionStage<E> decompose(CompletionStage<? extends CompletionStage<E>> d){
		return d.thenCompose((UnaryOperator<CompletionStage<E>>)SELF_RETURN);
	}
	private static final VarHandle POST_RUN,TRESHOLD;
	static {
		Lookup lk = MethodHandles.lookup();
		try{
			POST_RUN = lk.findVarHandle(CompletionTask.class, "postRun", Runnable.class);
			TRESHOLD = lk.findVarHandle(CompletionTask.class, "treshold", int.class);
		}catch(Exception e){
			throw new ExceptionInInitializerError(e);
		}
	}
	private volatile Object call;
	private volatile Runnable postRun;
	@SuppressWarnings("unused")
	private int treshold;
	CompletionTask(Object c,int t){
		call = c;
		treshold = t;
	}
	CompletionTask(){
		postRun = this;
	}
	public static CompletionTask<?> completeWithTreshold(int treshold){
		return treshold == 0 ? COMPLETED_TASK : new CompletionTask<>(null, treshold);
	}
	public static CompletionTask<?> runWithTreshold(Runnable rn,int treshold){
		if(treshold == 0){
			rn.run();
			return COMPLETED_TASK;
		}
		return new CompletionTask<>(rn, treshold);
	}
	public static <E> CompletionTask<E> supplyWithTreshold(Supplier<E> rn,int treshold){
		if(treshold == 0){
			CompletionTask<E> ts = new CompletionTask<>();
			ts.call = rn.get();
			return ts;
		}
		return new CompletionTask<>(rn, treshold);
	}
	public static CompletionTask<Void> runAsync(Runnable rn,Executor e){
		CompletionTask<Void> task = new CompletionTask<>(rn, 1);
		e.execute(task);
		return task;
	}
	public static <E> CompletionTask<E> supplyAsync(Supplier<E> rn,Executor e){
		CompletionTask<E> task = new CompletionTask<>(rn, 1);
		e.execute(task);
		return task;
	}
	@Override
	public void run() {
		int x = (int)TRESHOLD.getAndAdd(this,-1);
		if(x == 1){
			if(call instanceof Runnable){
				((Runnable)call).run();
				call = null;
			}else if(call instanceof Supplier<?>){
				call = ((Supplier<?>)call).get();
			}else if(call instanceof Consumer){
				((Consumer<?>) call).accept(null);
				call = null;
			}else if(call instanceof Function){
				call = ((Function<?,?>) call).apply(null);
			}else{
				call = null;
			}
			Runnable rn = (Runnable)POST_RUN.getAndSet(this,this);
			if(rn != null){
				rn.run();
			}
		}
	}
	@Override
	@SuppressWarnings("unchecked")
	public void accept(Object e){
		int x = (int)TRESHOLD.getAndAdd(this,-1);
		if(x == 1){
			if(call instanceof Consumer){
				((Consumer<Object>)call).accept(e);
				call = null;
			}else if(call instanceof Function){
				call = ((Function<Object,?>)call).apply(e);
			}else if(call instanceof Runnable){
				((Runnable) call).run();
				call = null;
			}else if(call instanceof Supplier<?>){
				call = ((Supplier<?>) call).get();
			}else{
				call = e;
			}
			Runnable rn = (Runnable)POST_RUN.getAndSet(this,this);
			if(rn != null){
				rn.run();
			}
		}
	}
	private <U extends Runnable> U x(U u){
		Object o = POST_RUN.compareAndExchange(this,null,u);
		if(o != null){
			if(o != this){
				throw new IllegalStateException();
			}
			u.run();
		}
		return u;
	}
	@SuppressWarnings("unchecked")
	private static <U> CompletionTask<U> toTask(CompletionStage<U> s){
		if(s == null){
			return (CompletionTask<U>)COMPLETED_TASK;
		}
		if(s instanceof CompletionTask){
			return (CompletionTask<U>)s;
		}
		CompletionTask<U> task = new CompletionTask<>(null, 0);
		s.thenAccept(task);
		return task;
	}
	private <U extends Runnable> U x(U u,Executor e){
		if(postRun == this){
			e.execute(u);
		}else{
			Object o = POST_RUN.compareAndExchange(this,null,(Runnable)() -> e.execute(u));
			if(o != null){
				if(o != this){
					throw new IllegalStateException();
				}
				e.execute(u);
			}
		}
		return u;
	}
	private static <U extends Runnable> U x(U u,CompletionStage<?> s){
		if(s instanceof CompletionTask){
			((CompletionTask<?>)s).x(u);
		}else{
			s.thenRun(u);
		}
		return u;
	}
	@SuppressWarnings("unchecked")
	public E get(){
		return (E)call;
	}
	@Override
	public <U> CompletionStage<U> thenApply(Function<? super E, ? extends U> fn) {
		return x(new CompletionTask<>((Supplier<U>)() -> fn.apply(get()), 1));
	}
	@Override
	public <U> CompletionStage<U> thenApplyAsync(Function<? super E, ? extends U> fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <U> CompletionStage<U> thenApplyAsync(Function<? super E, ? extends U> fn, Executor executor) {
		return x(new CompletionTask<>((Supplier<U>)() -> fn.apply(get()), 1),executor);
	}
	@Override
	public CompletionStage<Void> thenAccept(Consumer<? super E> action) {
		return x(new CompletionTask<Void>((Runnable)() -> action.accept(get()), 1));
	}
	@Override
	public CompletionStage<Void> thenAcceptAsync(Consumer<? super E> action) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<Void> thenAcceptAsync(Consumer<? super E> action, Executor executor) {
		return x(new CompletionTask<>((Runnable)() -> action.accept(get()), 1),executor);
	}
	@Override
	public CompletionStage<Void> thenRun(Runnable action) {
		return x(new CompletionTask<Void>(action,1));
	}
	@Override
	public CompletionStage<Void> thenRunAsync(Runnable action) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor) {
		return x(new CompletionTask<Void>(action,1),executor);
	}
	@Override
	public <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,BiFunction<? super E, ? super U, ? extends V> fn) {
		return thenCombine(toTask(other),fn);
	}
	public <U, V> CompletionStage<V> thenCombine(CompletionTask<? extends U> other,BiFunction<? super E, ? super U, ? extends V> fn) {
		return x(other.x(new CompletionTask<>((Supplier<V>)() -> fn.apply(get(),other.get()),2)));
	}
	@Override
	public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super E, ? super U, ? extends V> fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,BiFunction<? super E, ? super U, ? extends V> fn, Executor executor) {
		return thenCombineAsync(toTask(other),fn,executor);
	}
	public <U, V> CompletionStage<V> thenCombineAsync(CompletionTask<? extends U> other,BiFunction<? super E, ? super U, ? extends V> fn, Executor executor) {
		return x(x(new CompletionTask<Void>(null, 2),other)).x(new CompletionTask<V>((Supplier<V>)() -> fn.apply(get(), other.get()),1), executor);
	}
	@Override
	public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,BiConsumer<? super E, ? super U> action) {
		return thenAcceptBoth(toTask(other),action);
	}
	public <U> CompletionStage<Void> thenAcceptBoth(CompletionTask<? extends U> other,BiConsumer<? super E, ? super U> action) {
		return x(other.x(new CompletionTask<Void>((Runnable)() -> action.accept(get(),other.get()),2)));
	}
	@Override
	public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super E, ? super U> action) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,BiConsumer<? super E, ? super U> action, Executor executor) {
		return thenAcceptBothAsync(toTask(other),action,executor);
	}
	public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionTask<? extends U> other,BiConsumer<? super E, ? super U> action, Executor executor) {
		return x(other.x(new CompletionTask<Void>(null, 2))).x(new CompletionTask<Void>((Runnable)() -> action.accept(get(), other.get()),1), executor);
	}
	@Override
	public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
		return runAfterBoth((CompletionTask<?>)other,action);
	}
	public CompletionStage<Void> runAfterBoth(CompletionTask<?> other, Runnable action) {
		return x(other.x(new CompletionTask<Void>(action,2)));
	}
	@Override
	public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
		return runAfterBothAsync(toTask(other),action,executor);
	}
	public CompletionStage<Void> runAfterBothAsync(CompletionTask<?> other, Runnable action, Executor executor) {
		return x(other.x(new CompletionTask<Void>(null, 2))).x(new CompletionTask<Void>(action,1), executor);
	}
	@Override
	public <U> CompletionStage<U> applyToEither(CompletionStage<? extends E> other, Function<? super E, U> fn) {
		if(other instanceof CompletionTask){
			return applyToEither((CompletionTask<? extends E>)other,fn);
		}
		CompletionTask<U> task = new CompletionTask<>(fn, 1);
		x(() -> task.accept(get()));
		other.thenAccept(task);
		return task;
	}
	public <U> CompletionStage<U> applyToEither(CompletionTask<? extends E> other, Function<? super E, U> fn) {
		CompletionTask<U> task = new CompletionTask<>(fn, 1);
		x(() -> task.accept(get()));
		other.x(() -> task.accept(other.get()));
		return task;
	}
	@Override
	public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends E> other, Function<? super E, U> fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends E> other, Function<? super E, U> fn,Executor executor) {
		return applyToEitherAsync(toTask(other), fn, executor);
	}
	public <U> CompletionStage<U> applyToEitherAsync(CompletionTask<? extends E> other, Function<? super E, U> fn,Executor executor) {
		CompletionTask<E> task = new CompletionTask<>(null, 1);
		x(() -> task.accept(get()));
		other.x(() -> task.accept(other.get()));
		return task.x(new CompletionTask<U>((Supplier<U>)() -> fn.apply(task.get()), 1),executor);
	}
	@Override
	public CompletionStage<Void> acceptEither(CompletionStage<? extends E> other, Consumer<? super E> fn) {
		return acceptEither(toTask(other),fn);
	}
	public CompletionStage<Void> acceptEither(CompletionTask<? extends E> other, Consumer<? super E> fn) {
		CompletionTask<Void> task = new CompletionTask<>(fn, 1);
		x(() -> task.accept(get()));
		other.x(() -> task.accept(other.get()));
		return task;
	}
	@Override
	public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends E> other, Consumer<? super E> fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends E> other, Consumer<? super E> fn,Executor executor) {
		return acceptEitherAsync(toTask(other), fn, executor);
	}
	public CompletionStage<Void> acceptEitherAsync(CompletionTask<? extends E> other, Consumer<? super E> fn,Executor executor) {
		CompletionTask<E> task = new CompletionTask<>(null, 1);
		x(() -> task.accept(get()));
		other.x(() -> task.accept(other.get()));
		return task.x(new CompletionTask<Void>((Runnable)() -> fn.accept(task.get()), 1),executor);
	}
	@Override
	public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable fn) {
		return  runAfterEither(toTask(other),fn);
	}
	public CompletionStage<Void> runAfterEither(CompletionTask<?> other, Runnable fn) {
		return x(other.x(new CompletionTask<>(fn, 1)));
	}
	@Override
	public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable fn,Executor executor) {
		return runAfterEitherAsync(toTask(other), fn, executor);
	}
	public CompletionStage<Void> runAfterEitherAsync(CompletionTask<?> other, Runnable fn, Executor executor) {
		CompletionTask<E> task = new CompletionTask<>(null, 1);
		x(() -> task.accept(get()));
		other.x(() -> task.accept(other.get()));
		return task.x(new CompletionTask<Void>(fn, 1),executor);
	}
	@Override
	public <U> CompletionStage<U> thenCompose(Function<? super E, ? extends CompletionStage<U>> fn) {
		CompletionTask<U> task = new CompletionTask<>(null, 1);
		x(() -> fn.apply(get()).thenAccept(task));
		return task;
	}
	@Override
	public <U> CompletionStage<U> thenComposeAsync(Function<? super E, ? extends CompletionStage<U>> fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <U> CompletionStage<U> thenComposeAsync(Function<? super E, ? extends CompletionStage<U>> fn,Executor executor) {
		CompletionTask<U> task = new CompletionTask<>(null, 1);
		x(() -> fn.apply(get()).thenAccept(task),executor);
		return task;
	}
	@Override
	public <U> CompletionStage<U> handle(BiFunction<? super E, Throwable, ? extends U> fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <U> CompletionStage<U> handleAsync(BiFunction<? super E, Throwable, ? extends U> fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	public <U> CompletionStage<U> handleAsync(BiFunction<? super E, Throwable, ? extends U> fn, Executor executor) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<E> whenComplete(BiConsumer<? super E, ? super Throwable> action) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<E> whenCompleteAsync(BiConsumer<? super E, ? super Throwable> action) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<E> whenCompleteAsync(BiConsumer<? super E, ? super Throwable> action, Executor executor) {
		throw new UnsupportedOperationException();
	}
	@Override
	public CompletionStage<E> exceptionally(Function<Throwable, ? extends E> fn) {
		throw new UnsupportedOperationException();
	}
	@Override
	@SuppressWarnings("unchecked")
	public CompletableFuture<E> toCompletableFuture() {
		CompletableFuture<E> ft = new CompletableFuture<>();
		if(postRun == this){
			ft.complete((E)call);
		}else{
			x(() -> ft.complete((E)call));
		}
		return ft;
	}
}
