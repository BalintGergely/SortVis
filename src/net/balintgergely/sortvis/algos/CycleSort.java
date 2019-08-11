package net.balintgergely.sortvis.algos;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

public class CycleSort extends ConfigurableSorter{
	public CycleSort() {
	}
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		int index = 0;
		vis.setColor(index, 0xFF0000FF);
		while(true){
			int target = index;
			for(int x = index+1;x < vis.size;x++){
				if(vis.getColor(x) != 0xFF00FF00 && vis.compare(index, x) > 0){
					if(vis.getColor(target) == 0xFF00FFFF){
						vis.setColor(target, 0);
					}
					do{
					target++;
					}while(vis.getColor(target) == 0xFF00FF00);
					vis.setColor(target, 0xFF00FFFF);
				}
			}
			vis.setColor(target, 0xFF00FF00);
			if(target == index){
				do{
					index++;
					if(index >= vis.size){
						return CompletionTask.COMPLETED_TASK;
					}
				}while(vis.getColor(index) == 0xFF00FF00);
				vis.setColor(index, 0xFF0000FF);
			}else{
				vis.swap(target, index);
			}
		}
	}
	@Override
	public String toString(){
		return "CycleSort";
	}
}
