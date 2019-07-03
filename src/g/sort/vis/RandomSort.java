package g.sort.vis;

import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

public class RandomSort extends ConfigurableSorter {
	private volatile boolean intervention;
	public RandomSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		Random rng = ThreadLocalRandom.current();
		int z = vis.size*(vis.size-1)/2;
		double x = z/(double)vis.size;
		while(true){
			double v = 0;
			int aix = 0;
			for(int i = 0;i < z;i++){
				int a = rng.nextInt(vis.size-1);
				int b = rng.nextInt(vis.size);
				if(a >= b){
					int c = a;
					a = b;
					b = c+1;
				}
				vis.compareAndSwap(a, b);
				v++;
				if(v >= x && aix < vis.size){
					v -= x;
					vis.setColor(aix++, 0xBF007FFF);
				}
			}
			vis.clearVisuals();
			if(intervention){
				for(int i = 1;i < vis.size;i++){
					vis.compareAndSwap(i-1,i);
				}
			}
			if(vis.localCheck(true)){
				return CompletionTask.COMPLETED_STAGE;
			}
		}
	}
	public String toString(){
		return "RandomSort";
	}
	@Override
	public int getNumberOptions() {
		return 1;
	}
	@Override
	public String getOptionName(int index) {
		return "Bubble Intervention";
	}
	@Override
	public Class<?> getOptionClass(int index) {
		return Boolean.class;
	}
	@Override
	public Object[] getOptions(int index) {
		return null;
	}
	@Override
	public void setOption(int index, Object opt) {
		intervention = ((Boolean)opt).booleanValue();
	}
	@Override
	public Object getOption(int index) {
		return Boolean.valueOf(intervention);
	}
}
