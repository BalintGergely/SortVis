package g.sort.vis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 * An emergency loader used when java.util.ServiceLoader defects.
 */
public class EmergencyLoader implements Iterable<ConfigurableSorter>{
	private ArrayList<ConfigurableSorter> cache = new ArrayList<>(count());
	@Override
	public Iterator<ConfigurableSorter> iterator() {
		return new Iterator<ConfigurableSorter>(){
			private int i = 0;
			public boolean hasNext(){
				return i < count();
			}
			@Override
			public ConfigurableSorter next() {
				if(i == cache.size()){
					synchronized(cache){
						if(i == cache.size())
						cache.add(nThSorter(i));
					}
				}
				return cache.get(i++);
			}
		};
	}
	private static int count(){
		return 8;
	}
	private static ConfigurableSorter nThSorter(int n){
		switch(n){
		case 0:return new SelectionSort();
		case 1:return new InsertionSort();
		case 2:return new BubbleSort();
		case 3:return new OddEvenSort();
		case 4:return new MergeSort();
		case 5:return new QuickSort();
		case 6:return new HeapSort();
		case 7:return new RadixSort();
		default:throw new NoSuchElementException();
		}
	}
	public String toString(){
		return "Random (!)";
	}
}
