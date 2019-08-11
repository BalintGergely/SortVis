package net.balintgergely.sortvis.algos;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

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
		return CompletionTask.COMPLETED_TASK;
	}
	@Override
	public String toString(){
		return "Odd-Even Sort";
	}
}
