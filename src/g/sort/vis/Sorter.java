package g.sort.vis;

import java.util.Collections;
import java.util.Iterator;

@FunctionalInterface
public interface Sorter extends Iterable<Sorter>{
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
	public default Iterator<Sorter> iterator(){return Collections.emptyIterator();}
	public default int getNumberOptions(){return 0;};
	public default String getOptionName(int index){return null;}
	public default Class<?> getOptionClass(int index){return null;}
	public default Object[] getOptions(int index){return null;}
	public default void setOption(int index,Object opt){};
	public default Object getOption(int index){return null;}
}
