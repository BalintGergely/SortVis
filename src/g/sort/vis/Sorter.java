package g.sort.vis;

public interface Sorter {
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
	 */
	public void sort(VisualArray vis,Sorter srt);
	/**
	 * Sorts both visual arrays using this Sorter.
	 */
	public default void sort(VisualArray vis0,VisualArray vis1,Sorter srt){
		if(vis0.overlaps(vis1)){
			throw new IllegalArgumentException();
		}
		sort(vis0,srt);
		sort(vis1,srt);
	}
	public default void sort(VisualArray vis0,VisualArray vis1,VisualArray vis2,Sorter srt){
		if(vis0.overlaps(vis1) || vis1.overlaps(vis2) || vis2.overlaps(vis0)){
			throw new IllegalArgumentException();
		}
		sort(vis0,srt);
		sort(vis1,srt);
		sort(vis2,srt);
	}
}
