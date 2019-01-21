package g.sort.vis;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class GnomeSort extends ConfigurableSorter{
	public GnomeSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		int i = 0;
		while(i < vis.size){
			if(i > 0 && (vis.compareAndSwap(i-1, i) > 0)){
				i--;
			}else{
				i++;
			}
		}
		return CompletionTask.COMPLETED_STAGE;
	}
	public String toString(){
		return "Gnome Sort";
	}
}
