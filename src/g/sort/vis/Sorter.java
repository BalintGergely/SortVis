package g.sort.vis;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public interface Sorter {
	public static final CompletionStage<?> COMPLETED_STAGE = CompletableFuture.completedStage(null);
	public static final Runnable NULL_RUN = () -> {};
	public static CompletionStage<?> combine(CompletionStage<?> a,CompletionStage<?> b,Runnable rn){
		if(a == COMPLETED_STAGE){
			if(b == COMPLETED_STAGE){
				rn.run();
				return COMPLETED_STAGE;
			}
			return rn == NULL_RUN ? b : b.thenRun(rn);
		}
		if(b == COMPLETED_STAGE){
			return rn == NULL_RUN ? a : a.thenRun(rn);
		}
		return a.runAfterBoth(b, rn);
	}
	public static CompletionStage<?> combine(CompletionStage<?> a,CompletionStage<?> b,CompletionStage<?> c,Runnable rn){
		if(a == COMPLETED_STAGE){
			return combine(b,c,rn);
		}
		if(b == COMPLETED_STAGE){
			return combine(a,c,rn);
		}
		if(c == COMPLETED_STAGE){
			return combine(a,b,rn);
		}
		return a.runAfterBoth(b, NULL_RUN).runAfterBoth(c, rn);
	}
	public static boolean guardedSort(VisualArray vis){
		switch(vis.size){
		case 3:vis.compareAndSwap(0, 1);
				if(vis.compareAndSwap(1, 2) < 0){
					break;
				}
		case 2:vis.compareAndSwap(0, 1);
		case 1:
		case 0:return true;
		}
		return false;
	}
	/**
	 * Sorts the visual array using this Sorter. Recursive calls within the sorter objects are normally not allowed, so they should be done on the parameter <code>srt</code>
	 * @param exe TODO
	 * @return TODO
	 */
	public CompletionStage<?> sort(VisualArray vis,Sorter srt, Executor exe);
}
