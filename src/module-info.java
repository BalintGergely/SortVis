import g.sort.vis.BubbleSort;
import g.sort.vis.HeapSort;
import g.sort.vis.MergeSort;
import g.sort.vis.OddEvenSort;
import g.sort.vis.QuickSort;
import g.sort.vis.RadixSort;
import g.sort.vis.SelectionSort;
import g.sort.vis.Sorter;
import g.sort.vis.InsertionSort;

/**
 * 
 */
/**
 * @author balintgergely
 *
 */
module g.sort.vis {
	requires java.desktop;
	uses Sorter;
	provides Sorter with SelectionSort,InsertionSort,BubbleSort,OddEvenSort,MergeSort,QuickSort,HeapSort,RadixSort;
}