package g.sort.vis;

public class BitonicSort extends ConfigurableSorter{
	@Override
	public void sort(VisualArray vis, Sorter srt) {
		preSort(vis,false);
	}
	public void preSort(VisualArray vis,boolean direction){
		if(vis.size > 1){
			int mid = vis.size/2;
			VisualArray left = vis.subArray(0, mid),right = vis.subArray(mid, vis.size-mid);
			preSort(left,!direction);
			preSort(right,direction);
			subSort(vis,direction);
		}
	}
	public void subSort(VisualArray vis,boolean direction){
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
			subSort(vis.subArray(0, dis),direction);
			subSort(vis.subArray(dis, vis.size-dis),direction);
		}
	}
	public String toString(){
		return "Bitonic Sort";
	}
}
