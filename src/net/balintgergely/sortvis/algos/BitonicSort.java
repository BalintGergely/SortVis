package net.balintgergely.sortvis.algos;

import static net.balintgergely.sortvis.CompletionTask.*;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

public class BitonicSort extends ConfigurableSorter{
	private boolean basic = false;
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		return preSort(vis,false,exe);
	}
	public CompletionStage<?> preSort(VisualArray vis,boolean direction,Executor exe){
		if(vis.size > 1){
			int mid = vis.size/2;
			VisualArray left = vis.subArray(0, mid),right = vis.subArray(mid, vis.size-mid);
			if(vis.size <= 4 || exe == null){
				preSort(left,!direction,null);
				preSort(right,direction,null);
				return subSort(vis,direction,exe);
			}else{
				CompletionStage<?> a = decompose(supplyAsync(() -> preSort(left,!direction,exe), exe));
				CompletionStage<?> b = decompose(supplyAsync(() -> preSort(right,direction,exe), exe));
				return decompose(a.thenCombine(b, (Object x,Object y) -> subSort(vis,direction,exe)));
			}
		}
		return COMPLETED_TASK;
	}
	public CompletionStage<?> subSort(VisualArray vis,boolean direction,Executor exe){
		if(vis.size > 1){
			int dis = Integer.highestOneBit(vis.size-1);
			int i = 0;
			while(i < vis.size-dis){
				if(direction){
					vis.compareAndSwap(i+dis, i);
				}else{
					vis.compareAndSwap(i, i+dis);
				}
				i++;
			}
			if(vis.size <= 4 || exe == null){
				subSort(vis.subArray(0, dis),direction,null);
				subSort(vis.subArray(dis, vis.size-dis),direction,null);
			}else{
				return	combine(
						decompose(CompletionTask.supplyAsync(() -> subSort(vis.subArray(0, dis),direction,exe),exe)),
						decompose(CompletionTask.supplyAsync(() -> subSort(vis.subArray(dis, vis.size-dis),direction,exe),exe)),
						null);
			}
		}
		return COMPLETED_TASK;
	}
	@Override
	public int getNumberOptions() {
		return 0;
	}
	@Override
	public String getOptionName(int index) {
		return "Alternative Mode";
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
		basic = !((Boolean)opt).booleanValue();
	}
	@Override
	public Object getOption(int index) {
		return Boolean.valueOf(!basic);
	}
	@Override
	public String toString(){
		return "Bitonic Sort";
	}
}
