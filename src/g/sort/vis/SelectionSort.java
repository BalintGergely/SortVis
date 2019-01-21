package g.sort.vis;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class SelectionSort extends ConfigurableSorter{
	private boolean binary = false;
	public SelectionSort() {}
	public CompletionStage<?> sort(VisualArray vis,Sorter srt, Executor exe) {
		int a = 0,b = vis.size;
		while(a < b){
			if(a > 0)vis.setColor(a-1, 0xff00ff00);
			boolean bin = binary;
			int s = a,l = a;
			for(int t = a;t < b;t++){
				if(bin){
					switch(vis.compare(t, s, l)){
					case -1:s = t;break;
					case 1:l = t;break;
					}
				}else if(vis.compare(t, s) < 0){
					s = t;
				}
			}
			if(s != l){
				if(bin){
					if(b < vis.size)vis.setColor(b, 0);
					b--;
					vis.setColor(b, 0xff00ff00);
					if(b == s){
						s = l;
					}
					vis.swap(l, b);
				}
				vis.swap(s, a);
			}
			if(a > 0)vis.setColor(a-1, 0);
			a++;
		}
		if(b < vis.size)vis.setColor(b, 0);
		return CompletionTask.COMPLETED_STAGE;
	}
	@Override
	public int getNumberOptions() {
		return 1;
	}
	@Override
	public String getOptionName(int index) {
		return "Double Ended";
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
		binary = ((Boolean)opt).booleanValue();
	}
	@Override
	public Object getOption(int index) {
		return Boolean.valueOf(binary);
	}
	public String toString(){
		return "Selection Sort";
	}
}
