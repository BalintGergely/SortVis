package g.sort.vis;

public class GnomeSort extends ConfigurableSorter{
	public GnomeSort() {}
	@Override
	public void sort(VisualArray vis, Sorter srt) {
		int i = 0;
		while(i < vis.size){
			if(i > 0 && (vis.compareAndSwap(i-1, i) > 0)){
				i--;
			}else{
				i++;
			}
		}
	}
	public String toString(){
		return "Gnome Sort";
	}
}
