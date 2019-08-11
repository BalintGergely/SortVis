package net.balintgergely.sortvis.algos;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

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
		return CompletionTask.COMPLETED_TASK;
	}
	@Override
	public String toString(){
		return "Gnome Sort";
	}
}
