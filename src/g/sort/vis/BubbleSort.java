package g.sort.vis;

public class BubbleSort implements Sorter{
	private boolean binary = false;
	public BubbleSort() {}
	public void sort(VisualArray vis,Sorter str) {
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
	public String toString(){
		return "Bubble Sort";
	}
}
