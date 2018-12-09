import g.sort.vis.BitonicSort;
import g.sort.vis.BubbleSort;
import g.sort.vis.ConfigurableSorter;
import g.sort.vis.GnomeSort;
import g.sort.vis.GravitySort;
import g.sort.vis.HeapSort;
import g.sort.vis.MergeSort;
import g.sort.vis.OddEvenSort;
import g.sort.vis.QuickSort;
import g.sort.vis.RadixSort;
import g.sort.vis.SelectionSort;
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
	uses ConfigurableSorter;
	provides ConfigurableSorter with SelectionSort,InsertionSort,GnomeSort,BubbleSort,OddEvenSort,MergeSort,QuickSort,HeapSort,RadixSort,GravitySort,BitonicSort;
}