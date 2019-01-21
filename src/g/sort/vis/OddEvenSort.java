package g.sort.vis;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class OddEvenSort extends ConfigurableSorter{
	public OddEvenSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis,Sorter srt, Executor exe) {
		int i = 1;
		boolean b;
		boolean d = true;
		do{b = false;
			while(i < vis.size){
				b = (vis.compareAndSwap(i-1, i) > 0) | b;
				i += 2;
			}
			b |= d;
			d = false;
			i = (i%2)+1;
		}while(b);
		return CompletionTask.COMPLETED_STAGE;
	}
	public String toString(){
		return "Odd-Even Sort";
	}
}
