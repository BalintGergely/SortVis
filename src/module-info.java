import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.algos.BitonicSort;
import net.balintgergely.sortvis.algos.BubbleSort;
import net.balintgergely.sortvis.algos.CombSort;
import net.balintgergely.sortvis.algos.CountingSort;
import net.balintgergely.sortvis.algos.CycleSort;
import net.balintgergely.sortvis.algos.GnomeSort;
import net.balintgergely.sortvis.algos.GravitySort;
import net.balintgergely.sortvis.algos.HeapSort;
import net.balintgergely.sortvis.algos.InsertionSort;
import net.balintgergely.sortvis.algos.MergeSort;
import net.balintgergely.sortvis.algos.OddEvenSort;
import net.balintgergely.sortvis.algos.QuickSort;
import net.balintgergely.sortvis.algos.RadixSort;
import net.balintgergely.sortvis.algos.SelectionSort;
import net.balintgergely.sortvis.algos.ShellSort;
import net.balintgergely.sortvis.algos.SmoothSort;

/**
 * 
 */
/**
 * @author balintgergely
 *
 */
module g.sort.vis {
	requires java.desktop;
	uses ConfigurableSorter;
	provides ConfigurableSorter with
	SelectionSort,
	InsertionSort,
	GnomeSort,
	BubbleSort,
	OddEvenSort,
	MergeSort,
	QuickSort,
	HeapSort,
	SmoothSort,
	RadixSort,
	CountingSort,
	CycleSort,
	ShellSort,
	CombSort,
	GravitySort,
	BitonicSort;
}