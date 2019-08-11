package net.balintgergely.sortvis.algos;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

public class CountingSort extends ConfigurableSorter {
	public CountingSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		int i = vis.min,dex = 0;
		while(i <= vis.max){
			if(vis.contains(false, i)){
				for(int x = 0;x < vis.size;x++){
					if(vis.getValue(x) == i){
						vis.setColor(true, dex, 0x80A0A0A0);
						vis.copy(false, x, true, dex++);
					}else{
						vis.pingValue(x);
					}
				}
			}
			i++;
		}
		for(int x = 0;x < vis.size;x++){
			vis.setColor(true, x, 0);
			vis.copy(true, x, false, x);
		}
		return CompletionTask.COMPLETED_TASK;
	}
	@Override
	public String toString(){
		return "CountingSort";
	}
}
