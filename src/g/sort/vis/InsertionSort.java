package g.sort.vis;

public class InsertionSort extends ConfigurableSorter{
	private boolean binary = false;
	public InsertionSort(){};
	@Override
	public void sort(VisualArray vis,Sorter srt) {
		int i = 1;
		while(i < vis.size){
			vis.setColor(i, 0xff00ff00);
			int x;
			sub: if(binary){
				int min = 0,max = i-1;
				while(min < max){
					int med = (max-min)/2+min;
					switch(vis.compare(med, i)){
					case -1:
						min = med+1;
						break;
					case 0:
						x = med;
						break sub;
					case 1:
						max = med-1;
						if(max < 0){
							x = 0;
							break sub;
						}
					}
				}
				x = max;
				if(vis.compare(x, i) < 0){
					x++;
				}
			}else{
				x = i;
				while(x > 0 && vis.compare(x-1, i) > 0){
					x--;
				}
			}
			vis.copy(false,i, true, i, 1);
			vis.copy(false,x, false, x+1, i-x);
			vis.copy(true ,i, false, x);
			vis.setColor(i, 0);
			i++;
		}
	}
	@Override
	public int getNumberOptions() {
		return 1;
	}
	@Override
	public String getOptionName(int index) {
		return "Binary Insertion";
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
		return "Insertion Sort";
	}
}
