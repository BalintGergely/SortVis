package g.sort.vis;

import java.util.Iterator;
import java.util.Objects;
/**
 * An emergency loader used when java.util.ServiceLoader defects.
 */
public class EmergencyLoader implements Iterable<ConfigurableSorter>{
	private static final Class<?>[] CLASSES = new Class<?>[] {
		SelectionSort.class,
		InsertionSort.class,
		GnomeSort.class,
		BubbleSort.class,
		OddEvenSort.class,
		MergeSort.class,
		QuickSort.class,
		HeapSort.class,
		RadixSort.class,
		GravitySort.class,
		BitonicSort.class,
		RandomSort.class
	};
	private static ConfigurableSorter nThSorter(int n){
		@SuppressWarnings("unchecked")
		Class<? extends ConfigurableSorter> cls = (Class<? extends ConfigurableSorter>)CLASSES[n];
		try {
			return Objects.requireNonNull((ConfigurableSorter)cls.getMethod("provider").invoke(null));
		} catch (NoSuchMethodException e) {
			
		} catch (Throwable e) {
			throw new Error(e);
		}
		try {
			return cls.getConstructor().newInstance();
		} catch (Throwable e) {
			throw new Error(e);
		}
	}
	private final ConfigurableSorter[] sorters;
	public EmergencyLoader(){
		sorters = new ConfigurableSorter[CLASSES.length];
		for(int i = 0;i < CLASSES.length;i++){
			sorters[i] = nThSorter(i);
		}
	}
	@Override
	public Iterator<ConfigurableSorter> iterator() {
		return new Iterator<ConfigurableSorter>(){
			private int i = 0;
			public boolean hasNext(){
				return i < sorters.length;
			}
			@Override
			public ConfigurableSorter next() {
				return sorters[i++];
			}
		};
	}
	public String toString(){
		return "Random (!)";
	}
}
