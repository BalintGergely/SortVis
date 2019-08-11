package net.balintgergely.sortvis.algos;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.Colors;
import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

public class SmoothSort extends ConfigurableSorter{
	public SmoothSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		for(int i = 0;i < vis.size;i++){
			int a = 3,b = 1,x = 30;
			while(a <= i){
				a = a+b+1;
				b = a-b-1;
				x += 30;
			}
			vis.setColor(i, Colors.HSVtoRGB(x,100,100,40));
			if(i != 0){
				siftDown(vis,i);
			}
		}
		int i = vis.size;
		while(i > 0){
			i--;
			vis.setColor(i, 0);
			int x = countOf(i);
			if(x > 1){
				x = countOf(i-1);
				siftDown(vis, i-x-1);
				siftDown(vis, i-1);
			}
		}
		return CompletionTask.COMPLETED_TASK;
	}
	private static void siftDown(VisualArray vis,int currentElement){
		int treeSize;
		insertionDown: while((treeSize = countOf(currentElement)) <= currentElement){
			int previousRoot = currentElement-treeSize;
			if(treeSize != 1){
				if(vis.compare(previousRoot, currentElement) > 0){
					int childB = currentElement-1;
					int childA = childB-countOf(childB);
					if(vis.compare(childB, childA) > 0){
						childA = childB;
					}
					if(vis.compare(previousRoot, childA) > 0){
						vis.swap(previousRoot, currentElement);
						currentElement = previousRoot;
						continue insertionDown;
					}
				}
			}else{
				if(vis.compareAndSwap(previousRoot,currentElement) > 0){
					currentElement = previousRoot;
					continue insertionDown;
				}
			}
			break insertionDown;
		}//Great. Now we just have to sift down currentRoot and we have treeSize as a hint.
		siftDown: while(treeSize != 1){
			int childB = currentElement-1;
			int childA = childB-countOf(childB);
			if(vis.compare(childB, childA) > 0){
				childA = childB;
			}
			if(vis.compareAndSwap(childA, currentElement) <= 0){
				break siftDown;
			}
			currentElement = childA;
			treeSize = countOf(currentElement);
		}
	}
	public static int countOf(int i){//number of descendants of the ith index +1
		i++;
		while(true){
			switch(i){
			case 1:return 1;
			case 2:return 1;
			}
			int a = 3,b = 1;
			while(a <= i){
				a = a+b+1;
				b = a-b-1;
			}
			if(b == i){
				return b;
			}
			i -= b;
		}
	}
	@Override
	public String toString(){
		return "SmoothSort";
	}
}
