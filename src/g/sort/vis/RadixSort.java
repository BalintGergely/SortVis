package g.sort.vis;

import java.util.Arrays;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class RadixSort extends ConfigurableSorter{
	private boolean inPlace = false;
	private int base = 2;
	public RadixSort() {}

	public CompletionStage<?> sort(VisualArray vis,Sorter srt, Executor exe) {
		int[] c = new int[base];
		int m = 1;
		while(m <= vis.max){
			int p = base;
			if(c.length < p){
				c = new int[p];
			}
			if(inPlace){
				sortInPlace(vis,c,m,p);
			}else{
				sortOutOfPlace(vis,c,m,p);
			}
			m *= p;
		}
		return CompletionTask.COMPLETED_STAGE;
	}
	public void sortOutOfPlace(VisualArray vis,int[] counts,int d,int p){
		Arrays.fill(counts, 0);
		for(int x = 0;x < vis.size;x++){
			int v = vis.getValue(x),i = (v / d) % p;
			counts[i]++;
			vis.setColor(true, x, Colors.HSVtoRGB(i*360/p, 50, 100, 70));
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
	 * @param vis
	 * @param counts
	 * @param view
	 */
	public void sortInPlace(VisualArray vis,int[] counts,int d,int p){
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
		return 2;
	}
	@Override
	public String getOptionName(int index) {
		switch(index){
		case 0:return "In place";
		case 1:return "Numeral base";
		default:throw new IllegalArgumentException();
		}
	}
	@Override
	public Class<?> getOptionClass(int index) {
		switch(index){
		case 0:return Boolean.class;
		case 1:return Integer.class;
		default:throw new IllegalArgumentException();
		}
	}
	@Override
	public Object[] getOptions(int index) {
		switch(index){
		case 1:return new Integer[]{2,Character.MAX_RADIX};
		default:return null;
		}
	}
	@Override
	public void setOption(int index, Object opt) {
		switch(index){
		case 0:inPlace = ((Boolean)opt).booleanValue();break;
		case 1:base = ((Integer)opt).intValue();break;
		}
	}
	@Override
	public Object getOption(int index) {
		switch(index){
		case 0:return Boolean.valueOf(inPlace);
		case 1:return Integer.valueOf(base);
		default:throw new IllegalArgumentException();
		}
	}
	public String toString(){
		return "Radix Sort";
	}
}
