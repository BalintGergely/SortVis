package net.balintgergely.sortvis.algos;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.Colors;
import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

public class RadixSort extends ConfigurableSorter{
	private boolean inPlace = false,msdf = false;
	private int base = 2;
	public RadixSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis,Sorter srt, Executor exe) {
		if(msdf){
			int max = vis.localMaximum(false);
			int p = base,d = 1;
			while(max/d >= p){
				d *= p;
			}
			check: while(true){
				int div = (vis.getValue(0)/d)%p;
				for(int i = 1;i < vis.size;i++){
					if((vis.getValue(i)/d)%p != div){
						break check;
					}
				}
				if(d == 1){//Every value appears to be equal in base p. They probably are all equal.
					return CompletionTask.COMPLETED_TASK;
				}
				d /= p;
			}
			int[] c = new int[p];
			if(inPlace){
				sortInPlace(vis, c, d, p);
				for(int i = 0;i < p;i++){
					c[i]++;
				}
			}else{
				sortOutOfPlace(vis, c, d, p);
			}
			CompletionTask<?> tsk = CompletionTask.completeWithTreshold(p);
			int a = 0;
			for(int i = 0;i < p;i++){
				int b = c[i];
				if(b == a){
					tsk.run();
				}else{
					VisualArray subVis = vis.subArray(a, b-a);
					CompletionTask.decompose(CompletionTask.supplyAsync(() -> srt.sort(subVis, this, exe), exe)).thenRun(tsk);
				}
				a = b;
			}
			return tsk;
		}
		int[] c = null;
		int m = 1;
		while(m <= vis.max){
			int p = base;
			if(c == null || c.length < p){
				c = new int[p];
			}
			if(inPlace){
				sortInPlace(vis,c,m,p);
			}else{
				sortOutOfPlace(vis,c,m,p);
			}
			m *= p;
		}
		return CompletionTask.COMPLETED_TASK;
	}
	private static void sortOutOfPlace(VisualArray vis,int[] counts,int d,int p){
		Arrays.fill(counts, 0);
		for(int x = 0;x < vis.size;x++){
			int v = vis.getValue(x),i = (v / d) % p;
			counts[i]++;
			vis.setColor(true, x, Colors.HSVtoRGB(i*360/p, 100, 100, 50));
			vis.copy(false, x, true, x);
		}
		int a = 0,b = 0;
		for(int x = 0;x < counts.length;x++){
			a += counts[x];
			counts[x] = b;
			b = a;
		}
		for(int x = 0;x < vis.size;x++){
			int v = vis.getValue(true,x),i = (v / d) % p;
			vis.setColor(true, x, 0);
			vis.copy(true, x, false, counts[i]++);
		}
	}
	/**
	 * In case anyone got here, I have to make a confession. This is not the fastest in-place radix sort implementation. It is partly designed to be entertaining.
	 */
	private static void sortInPlace(VisualArray vis,int[] counts,int d,int p){
		Arrays.fill(counts, vis.size-1);
		for(int x = 0;x < vis.size;x++){
			int value = vis.getValue(0),v = (value / d) % p;
			int t = counts[v];
			vis.copy(false, 0, true, 0, 1);
			vis.copy(1, 0, t);
			vis.copy(true, 0, false, t, 1);
			vis.pingValues(counts);
			vis.fireEvent(value, -1, true);
			while(v > 0){
				v--;
				counts[v]--;
			}
		}
	}
	@Override
	public int getNumberOptions() {
		return 3;
	}
	@Override
	public String getOptionName(int index) {
		switch(index){
		case 0:return "In place";
		case 1:return "Most significant digit first";
		case 2:return "Numeral base";
		default:throw new IllegalArgumentException();
		}
	}
	@Override
	public Class<?> getOptionClass(int index) {
		switch(index){
		case 0:
		case 1:return Boolean.class;
		case 2:return Integer.class;
		default:throw new IllegalArgumentException();
		}
	}
	@Override
	public Object[] getOptions(int index) {
		switch(index){
		case 2:return new Integer[]{2,Integer.MAX_VALUE};
		default:return null;
		}
	}
	@Override
	public void setOption(int index, Object opt) {
		switch(index){
		case 0:inPlace = ((Boolean)opt).booleanValue();break;
		case 1:msdf = ((Boolean)opt).booleanValue();break;
		case 2:base = ((Integer)opt).intValue();break;
		}
	}
	@Override
	public Object getOption(int index) {
		switch(index){
		case 0:return Boolean.valueOf(inPlace);
		case 1:return Boolean.valueOf(msdf);
		case 2:return Integer.valueOf(base);
		default:throw new IllegalArgumentException();
		}
	}
	@Override
	public String toString(){
		return "Radix Sort";
	}
}
