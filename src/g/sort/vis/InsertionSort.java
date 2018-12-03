package g.sort.vis;

public class InsertionSort implements Sorter{
	public InsertionSort() {}
	@Override
	public void sort(VisualArray vis,Sorter srt) {
		int i = 1;
		while(i < vis.size){
			vis.setColor(i, 0xff00ff00);
			int x = i;
			while(x > 0 && vis.compare(x-1, i) > 0){
				x--;
			}
			vis.copy(false,i, true, x, 1);
			vis.copy(false,x, false, x+1, i-x);
			vis.copy(true ,x, false, x, 1);
			vis.setColor(i, 0);
			i++;
		}
	}
	public String toString(){
		return "Insertion Sort";
	}
}
