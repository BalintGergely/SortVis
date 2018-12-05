package g.sort.vis;

public class OddEvenSort extends ConfigurableSorter{
	public OddEvenSort() {}
	@Override
	public void sort(VisualArray vis,Sorter srt) {
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
	}
	public String toString(){
		return "Odd-Even Sort";
	}
}
