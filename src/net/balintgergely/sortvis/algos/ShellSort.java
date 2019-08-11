package net.balintgergely.sortvis.algos;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.Colors;
import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

public class ShellSort extends ConfigurableSorter{
	private volatile GapSequence SEQUENCE = GapSequence.SHELL;
	public ShellSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		SEQUENCE.iterate(vis.size, vis, ShellSort::sortWithGap);
		return CompletionTask.COMPLETED_TASK;
	}
	private static void sortWithGap(VisualArray vis,int gap){
		int s = vis.size-gap;
		if(s > 0){
			for(int i = 0;i < gap;i++){
				int color = gap == 1 ? 0 : Colors.HSVtoRGB(i*360f/gap,50,100,50);
				for(int z = i;z < vis.size;z += gap){
					vis.setColor(z,color);
				}
			}
			for(int i = 0;i < s;i++){
				int x = i;
				while(x >= 0 && vis.compareAndSwap(x, x+gap) > 0){
					x -= gap;
				}
			}
		}
	}
	@Override
	public int getNumberOptions() {
		return 1;
	}
	@Override
	public String getOptionName(int index) {
		return "Gap Sequence";
	}
	@Override
	public Class<?> getOptionClass(int index) {
		return GapSequence.class;
	}
	@Override
	public Object[] getOptions(int index) {
		return GapSequence.values();
	}
	@Override
	public void setOption(int index, Object opt) {
		SEQUENCE = (GapSequence)Objects.requireNonNull(opt);
	}
	@Override
	public Object getOption(int index) {
		return SEQUENCE;
	}
	@Override
	public String toString(){
		return "ShellSort";
	}
}
