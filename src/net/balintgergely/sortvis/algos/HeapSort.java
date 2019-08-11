package net.balintgergely.sortvis.algos;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import net.balintgergely.sortvis.Colors;
import net.balintgergely.sortvis.CompletionTask;
import net.balintgergely.sortvis.ConfigurableSorter;
import net.balintgergely.sortvis.Sorter;
import net.balintgergely.sortvis.VisualArray;

public class HeapSort extends ConfigurableSorter{
	public HeapSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis,Sorter srt, Executor exe) {
		//Step 1: Heapify: Find the lowest element that has children and sift down.
		//x's parent is (x-1)/2
		//x's children is x*2+1
		//x's children is x*2+2
		//so the first parent is (vis.size-2)/2
		if(rootFirst){
			int i = 0;
			while(i < vis.size){
				vis.setColor(i, colorOf(i));
				siftUp(vis, i);
				i++;
			}
		}else{
			int bp = (vis.size-2)/2,i = vis.size;
			while(i > bp){
				i--;
				vis.setColor(i, colorOf(i));
			}
			while(i > 0){
				siftDown(vis,i,vis.size);
				i--;
				vis.setColor(i, colorOf(i));
			}
		}
		//Step 2 & 3: Sort: 
		int len = vis.size;
		while(len > 0){
			siftDown(vis,0,len);
			len--;
			vis.setColor(len, 0);
			vis.swap(0, len);
		}
		return CompletionTask.COMPLETED_TASK;
	}
	public static void siftDown(VisualArray vis,int index,int len){
		while(true){
			int ca = (index*2)+1,cb = ca+1;
			if(ca < len){
				if(cb < len && vis.compare(ca, cb) < 0){
					ca = cb;
				}
				if(vis.compareAndSwap(ca,index) > 0){
					index = ca;
					continue;
				}
			}
			break;
		}
	}
	public static void siftUp(VisualArray vis,int index){
		while(index > 0){
			int t = (index-1)/2;
			if(vis.compareAndSwap(index, t) > 0){
				index = t;
			}else break;
		}
	}
	public static int colorOf(int index){
		int layer = Integer.numberOfTrailingZeros(Integer.highestOneBit(index+1));
		return Colors.HSVtoRGB(layer*45,50,100,50);
	}
	private boolean rootFirst = false;
	@Override
	public int getNumberOptions(){return 1;}
	@Override
	public String getOptionName(int index){
		return "Root first";
	}
	@Override
	public Class<?> getOptionClass(int index){
		return Boolean.class;
	}
	@Override
	public Object[] getOptions(int index){
		return null;
	}
	@Override
	public void setOption(int index,Object opt){
		switch(index){
		case 0:rootFirst = ((Boolean)opt).booleanValue();break;
		}
	}
	@Override
	public Object getOption(int index){
		switch(index){
		case 0:return Boolean.valueOf(rootFirst);
		}
		return null;
	}
	@Override
	public String toString(){
		return "Heap Sort";
	}
}
