package net.balintgergely.sortvis;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public interface Sorter {
	public static boolean guardedSort(VisualArray vis){
		switch(vis.size){
		case 3:vis.compareAndSwap(0, 1);
				if(vis.compareAndSwap(1, 2) < 0){
					break;
				}
			//$FALL-THROUGH$
		case 2:vis.compareAndSwap(0, 1);
			//$FALL-THROUGH$
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
	public default String description(){
		return null;
	}
}
