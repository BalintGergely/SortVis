package g.sort.vis;

public class HeapSort implements Sorter{
	public HeapSort() {}
	@Override
	public void sort(VisualArray vis,Sorter srt) {
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
	}
	public void siftDown(VisualArray vis,int index,int len){
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
	public void siftUp(VisualArray vis,int index){
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
	public int getNumberOptions(){return 1;};
	public String getOptionName(int index){
		switch(index){
		case 0:return "Root first";
		}
		return null;
	}
	public Class<?> getOptionClass(int index){
		switch(index){
		case 0:return Boolean.class;
		case 1:return String.class;
		}
		return null;
	}
	public Object[] getOptions(int index){
		switch(index){
		}
		return null;
	}
	public void setOption(int index,Object opt){
		switch(index){
		case 0:rootFirst = ((Boolean)opt).booleanValue();break;
		}
	};
	public Object getOption(int index){
		switch(index){
		case 0:return Boolean.valueOf(rootFirst);
		}
		return null;
	}
	public String toString(){
		return "Heap Sort";
	}
}
