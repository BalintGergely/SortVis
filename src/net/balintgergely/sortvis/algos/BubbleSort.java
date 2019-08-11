package net.balintgergely.sortvis.algos;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

public class BubbleSort extends ConfigurableSorter{
	private boolean binary = false;
	public BubbleSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis,Sorter str, Executor exe) {
		int a = 0,b = vis.size;
		while(a < b){
			boolean m = true;
			int x = a+1;
			while(x < b){
				m = (vis.compareAndSwap(x-1, x) <= 0) & m;
				x++;
			}
			if(m){
				break;
			}
			if(binary){
				a++;
				while(x > a){
					x--;
					vis.compareAndSwap(x-1, x);
				}
			}
			b--;
		}
		return CompletionTask.COMPLETED_TASK;
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
	@Override
	public String toString(){
		return "Bubble Sort";
	}
}
