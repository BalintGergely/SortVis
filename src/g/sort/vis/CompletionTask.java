package g.sort.vis;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class CompletionTask<E> implements Callable<E>,CompletionStage<E>{

	public CompletionTask() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public E call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> thenApply(Function<? super E, ? extends U> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> thenApplyAsync(Function<? super E, ? extends U> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> thenApplyAsync(Function<? super E, ? extends U> fn, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> thenAccept(Consumer<? super E> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> thenAcceptAsync(Consumer<? super E> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> thenAcceptAsync(Consumer<? super E> action, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> thenRun(Runnable action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> thenRunAsync(Runnable action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> thenRunAsync(Runnable action, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U, V> CompletionStage<V> thenCombine(CompletionStage<? extends U> other,
			BiFunction<? super E, ? super U, ? extends V> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super E, ? super U, ? extends V> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U, V> CompletionStage<V> thenCombineAsync(CompletionStage<? extends U> other,
			BiFunction<? super E, ? super U, ? extends V> fn, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<Void> thenAcceptBoth(CompletionStage<? extends U> other,
			BiConsumer<? super E, ? super U> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super E, ? super U> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<Void> thenAcceptBothAsync(CompletionStage<? extends U> other,
			BiConsumer<? super E, ? super U> action, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> runAfterBoth(CompletionStage<?> other, Runnable action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> runAfterBothAsync(CompletionStage<?> other, Runnable action, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> applyToEither(CompletionStage<? extends E> other, Function<? super E, U> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends E> other, Function<? super E, U> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> applyToEitherAsync(CompletionStage<? extends E> other, Function<? super E, U> fn,
			Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> acceptEither(CompletionStage<? extends E> other, Consumer<? super E> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends E> other, Consumer<? super E> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> acceptEitherAsync(CompletionStage<? extends E> other, Consumer<? super E> action,
			Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> runAfterEither(CompletionStage<?> other, Runnable action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<Void> runAfterEitherAsync(CompletionStage<?> other, Runnable action, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> thenCompose(Function<? super E, ? extends CompletionStage<U>> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> thenComposeAsync(Function<? super E, ? extends CompletionStage<U>> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> thenComposeAsync(Function<? super E, ? extends CompletionStage<U>> fn,
			Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> handle(BiFunction<? super E, Throwable, ? extends U> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> handleAsync(BiFunction<? super E, Throwable, ? extends U> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <U> CompletionStage<U> handleAsync(BiFunction<? super E, Throwable, ? extends U> fn, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<E> whenComplete(BiConsumer<? super E, ? super Throwable> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<E> whenCompleteAsync(BiConsumer<? super E, ? super Throwable> action) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<E> whenCompleteAsync(BiConsumer<? super E, ? super Throwable> action, Executor executor) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletionStage<E> exceptionally(Function<Throwable, ? extends E> fn) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CompletableFuture<E> toCompletableFuture() {
		// TODO Auto-generated method stub
		return null;
	}
}
