package g.sort.vis;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

public class QuickSort extends ConfigurableSorter{
	/*
	 *
	 * Quick sort
	 * -Take an unsorted sequence of numbers
	 * -Pick a pivot (First, last, middle, median)
	 * -Rearrange the other elements by comparing them with the pivot


	 * --LR method: LEFTA = 0+,RIGHTA = n-, Pivot is swapped with the rightmost element pre-iteration.
	 * ---Iterate LEFTA until higher than pivot is found.
	 * ---Iterate RIGHTA until lower than pivot is found.
	 * ---Swap LEFTA with RIGHTA. Repeat until LEFTA and RIGHTA collides.
	 * ---Swap the pivot with RIGHTA. Ignore it during recursion.

	 * --LL method: LEFTA = 0+,LEFTB = 0+, Pivot is swapped with the rightmost element pre-iteration.
	 * ---Iterate LEFTB until lower than pivot is found.
	 * ---Swap with LEFTA. Repeat until LEFTB reaches the pivot.
	 * ---Swap the pivot with LEFTA. Ignore it during recursion.

	 * --Ternary LR method: LEFTA = 0+,LEFTB = 0+,RIGHTA = n-,RIGHTB = n-, Pivot is swapped with the rightmost element pre-iteration.
	 * ---Iterate LEFTA swapping equals with LEFTB until higher than pivot is found.
	 * ---Iterate RIGHTA swapping equals with RIGHTB until lower than pivot is found.
	 * ---Swap LEFTA with RIGHTA. Repeat until LEFTA and RIGHTA collides.
	 * ---Copy LEFTB and RIGHTB to the middle along with the pivot. Ignore them during recursion.

	 * --Ternary LL method: LEFTA = 0+,LEFTB = 0+,RIGHTA = n-, Pivot is swapped with the rightmost element pre-iteration.
	 * ---Iterate LEFTB swapping equals with RIGHTA until lower than pivot is found.
	 * ---Swap with LEFTA. Repeat until LEFTB and RIGHTA collides.
	 * ---Copy RIGHTA to the middle along with the pivot. Ignore them during recursion.

	 * LR :	[sa>] [???] [<la] [ a ] -> [sa] [a] [la]
	 * LL :	[sa>] [la>] [???] [ a ] -> [sa] [a] [la]
	 * RR :	[ a ] [???] [<sa] [<la] -> [sa] [a] [la]

	 * TLR:	[=a=] [sa>] [???] [<la] [=a=] [ a ]	-> [sa] [=a] [la]
	 * TLL:	[sa>] [la>] [???] [=a=] [ a ]		-> [sa] [=a] [la]
	 * TRR:	[ a ] [=a=] [???] [<sa] [<la]		-> [sa] [=a] [la]
	 * 
	 * DUAL  : [ a ] [a-b] [sa>] [???] [<lb] [a-b] [ b ]	-> [sa] [a] [a-b] [b] [lb]
	 * DUAL L: [ a ] [sa>] [a-b] [???] [<lb] [ b ]			-> [sa] [a] [a-b] [b] [lb]
	 * DUAL R: [ a ] [sa>] [???] [a-b] [<lb] [ b ]			-> [sa] [a] [a-b] [b] [lb]
	 */
	public static final String	PIVOT_FIRST = "First element",PIVOT_LAST = "Last element",PIVOT_MIDDLE = "Middle element",PIVOT_MEDIAN = "Median of three",PIVOT_RANDOM = "Random";
	public QuickSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis,Sorter srt, Executor exe){
		return subSorters.get(ThreadLocalRandom.current().nextInt(9)).sort(vis, srt, exe);
	}
	private String pivotRule = PIVOT_MIDDLE;
	public void pickPivot(VisualArray vis,int off,int len,int target){
		switch(pivotRule){
		case PIVOT_FIRST:vis.swap(off,target);break;
		case PIVOT_LAST:vis.swap(off+len-1,target);break;
		case PIVOT_MIDDLE:vis.swap(off+len/2,target);break;
		case PIVOT_MEDIAN:int a = off,b = off+len/2,c = off+len-1;
			if(vis.compare(a, b) > 0){//			0aa	bb0	ccc
				if(vis.compare(a, c) > 0){// 		00a	bb0	cc0 els	0a0 b00 00c
					if(vis.compare(b, c) > 0){//	00a	0b0	c00 els 00a	b00	0c0
						a = b;
					}else{
						a = c;
					}
				}
			}else{//								aa0 0bb ccc
				if(vis.compare(a, c) < 0){//		a00 0bb 0cc els 0a0 00b c00
					if(vis.compare(b, c) < 0){//	a00 0b0 00c els a00 00b 0c0
						a = b;
					}else{
						a = c;
					}
				}
			}
			vis.swap(a,target);break;
		case PIVOT_RANDOM:vis.swap(off+ThreadLocalRandom.current().nextInt(len), target);break;
		default:throw new IllegalStateException();
		}
	}
	@Override
	public int getNumberOptions() {
		return 1;
	}
	@Override
	public String getOptionName(int index) {
		switch(index){
		case 0:return "Pivot rule";
		default:throw new IllegalArgumentException();
		}
	}
	@Override
	public Class<?> getOptionClass(int index) {
		return String.class;
	}
	@Override
	public Object[] getOptions(int index) {
		switch(index){
		case 0:return new String[]{PIVOT_FIRST,PIVOT_LAST,PIVOT_MIDDLE,PIVOT_MEDIAN,PIVOT_RANDOM};
		default:throw new IllegalArgumentException();
		}
	}
	@Override
	public void setOption(int index, Object opt) {
		switch(index){
		case 0:pivotRule = opt.toString();break;
		default:throw new IllegalArgumentException();
		}
	}
	@Override
	public Object getOption(int index) {
		switch(index){
		case 0:return pivotRule;
		default:throw new IllegalArgumentException();
		}
	}
	class AbstractSibling implements Sorter{
		public final String name;
		private Sorter sorter;
		private AbstractSibling(String n,Sorter srt){
			name = n;
			sorter = srt;
		}
		public String toString(){
			return name;
		}
		@Override
		public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
			return sorter.sort(vis, srt, exe);
		}
	}
	private static CompletionStage<?> sortTask(VisualArray vis,Sorter source,Sorter target,Executor exe){
		if(exe == null){
			return target.sort(vis, source, exe);
		}
		return Sorter.decompose(CompletableFuture.supplyAsync(() -> target.sort(vis, source, exe), exe));
	}
	private final Sorter
	SLR = new AbstractSibling("Twins Left-Right",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivot = vis.size-1;//Absolute
		pickPivot(vis, 0, vis.size, pivot);
		vis.setColor(pivot, 0xffffff00);
		int left = 0,right = vis.size-1;
		main: while(left < right){
			if(vis.compare(left, pivot) > 0){
				do{
					right--;
					if(left == right){
						break main;
					}
				}while(vis.compare(right, pivot) >= 0);
				vis.swap(right, left);
			}
			left++;
		}
		if(left != right){
			System.out.println("Err");
		}
		vis.setColor(pivot, 0);
		vis.swap(left, pivot);
		final int afin = left;
		vis.setColor(afin, 0xff00ff00);
		return Sorter.combine(
				sortTask(vis.subArray(0, left),this,srt,exe),
				sortTask(vis.subArray(left+1, vis.size-left-1),this,srt,exe),
				() -> vis.setColor(afin,0));
	}),
	SLL = new AbstractSibling("Twins Left-Left",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivot = vis.size-1;//Absolute
		pickPivot(vis, 0, vis.size, pivot);
		vis.setColor(pivot, 0xffffff00);
		int lefta = 0,leftb = 0,right = vis.size-1;
		while(leftb < right){
			if(vis.compare(leftb, pivot) < 0){
				vis.swap(leftb, lefta);
				lefta++;
			}
			leftb++;
		}
		vis.setColor(pivot, 0);
		vis.swap(lefta, pivot);
		final int afin = lefta;
		vis.setColor(afin, 0xff00ff00);
		return Sorter.combine(
				sortTask(vis.subArray(0, lefta),this,srt,exe),
				sortTask(vis.subArray(lefta+1, vis.size-lefta-1),this,srt,exe),
				() -> vis.setColor(afin, 0));
	}),
	SRR = new AbstractSibling("Twins Right-Right",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivot = 0;//Absolute
		pickPivot(vis, 0, vis.size, pivot);
		vis.setColor(pivot, 0xffffff00);
		int righta = vis.size,rightb = vis.size;
		while(righta > 1){
			righta--;
			if(vis.compare(righta, pivot) > 0){
				rightb--;
				vis.swap(righta, rightb);
			}
		}
		rightb--;
		vis.setColor(pivot, 0);
		vis.swap(rightb, pivot);
		final int afin = rightb;
		vis.setColor(afin, 0xff00ff00);
		return Sorter.combine(
				sortTask(vis.subArray(rightb+1, vis.size-rightb-1),this,srt,exe),
				sortTask(vis.subArray(0, rightb),this,srt,exe),
				() -> vis.setColor(afin, 0));
	}),
	TLR = new AbstractSibling("Ternary Left-Right",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivot = vis.size-1;//Absolute
		pickPivot(vis, 0, vis.size, pivot);
		vis.setColor(pivot, 0xffffff00);
		int lefta = 0,leftb = 0,righta = vis.size-1,rightb = righta;
		main: while(leftb < righta){
			switch(vis.compare(leftb, pivot)){
			case 0:vis.setColor(lefta, 0xffffff00);vis.swap(leftb, lefta);lefta++;break;
			case 1:sub:while(true){
						righta--;
						if(leftb >= righta){
							break main;
						}
						switch(vis.compare(righta, pivot)){
						case 0:rightb--;vis.setColor(rightb, 0xffffff00);vis.swap(righta, rightb);break;
						case -1:break sub;
						}
					}
					vis.swap(leftb, righta);
			}
			leftb++;
		}
		while(lefta > 0){
			lefta--;
			leftb--;
			vis.setColor(lefta, 0);
			vis.swap(lefta, leftb);
		}
		while(rightb < vis.size){
			vis.setColor(rightb, 0);
			vis.swap(righta, rightb);
			righta++;
			rightb++;
		}
		final int afin = leftb,bfin = righta-1;
		vis.setColor(afin, 0xff00ff00);
		vis.setColor(bfin, 0xff00ff00);
		return Sorter.combine(
				sortTask(vis.subArray(0, leftb),this,srt,exe),
				sortTask(vis.subArray(righta, vis.size-righta),this,srt,exe),
				() -> {
					vis.setColor(afin, 0);
					vis.setColor(bfin, 0);
				});
	}),
	TLL = new AbstractSibling("Ternary Left-Left",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivot = vis.size-1;//Absolute
		pickPivot(vis, 0, vis.size, pivot);
		vis.setColor(pivot, 0xffffff00);
		int lefta = 0,leftb = 0,right = vis.size-1;
		while(leftb < right){
			switch(vis.compare(leftb, pivot)){
			case -1:vis.swap(leftb, lefta);lefta++;leftb++;break;
			case 0:right--;vis.setColor(right, 0xffffff00);vis.swap(leftb, right);break;
			case 1:leftb++;break;
			}
		}
		leftb = lefta;
		while(right < vis.size){
			vis.setColor(right, 0);
			vis.swap(leftb, right);
			right++;
			leftb++;
		}
		final int afin = lefta,bfin = leftb-1;
		vis.setColor(afin, 0xff00ff00);
		vis.setColor(bfin, 0xff00ff00);
		return Sorter.combine(
				sortTask(vis.subArray(0, lefta),this,srt,exe),
				sortTask(vis.subArray(leftb, vis.size-leftb),this,srt,exe),
				() -> {
					vis.setColor(afin, 0);
					vis.setColor(bfin, 0);
				});
	}),
	TRR = new AbstractSibling("Ternary Right-Right",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivot = 0;//Absolute
		pickPivot(vis, 0, vis.size, pivot);
		vis.setColor(pivot, 0xffffff00);
		int left = 1,righta = vis.size-1,rightb = righta;
		while(left <= righta){
			switch(vis.compare(righta, pivot)){
			case -1:righta--;break;
			case 0:vis.swap(righta, left);vis.setColor(left, 0xffffff00);left++;break;
			case 1:vis.swap(rightb, righta);rightb--;righta--;
			}
		}
		righta = rightb;
		while(left > 0){
			left--;
			vis.setColor(left, 0);
			vis.swap(left, righta);
			righta--;
		}
		final int afin = righta+1,bfin = rightb;
		vis.setColor(afin, 0xff00ff00);
		vis.setColor(bfin, 0xff00ff00);
		return Sorter.combine(
				sortTask(vis.subArray(rightb+1, vis.size-rightb-1),this,srt,exe),
				sortTask(vis.subArray(0, righta+1),this,srt,exe),
				() -> {
					vis.setColor(afin, 0);
					vis.setColor(bfin, 0);
				});
	}),
	DLR = new AbstractSibling("Dual pivot Left-Right",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivota = 0,pivotb = vis.size-1;//Absolute
		pickPivot(vis, 0, vis.size, pivota);
		vis.setColor(pivota, 0xffffff00);
		pickPivot(vis, 1,vis.size-1, pivotb);
		int r = vis.compareAndSwap(pivota, pivotb);// a < b
		vis.setColor(pivotb, 0xffffff00);
		int lefta = 1,leftb = 1,righta = vis.size-1,rightb = righta;
		main: while(leftb < righta){
			switch(vis.compare(leftb, pivota, pivotb)){
			case 0:vis.swap(leftb, lefta);lefta++;break;
			case 1:sub:while(true){
						righta--;
						if(leftb >= righta){
							break main;
						}
						switch(vis.compare(righta, pivota, pivotb)){
						case 0:rightb--;vis.swap(righta, rightb);break;
						case -1:break sub;
						}
					}
					vis.swap(leftb, righta);
			}
			leftb++;
		}
		while(lefta > 0){
			lefta--;
			leftb--;
			vis.swap(lefta, leftb);
		}
		while(rightb < vis.size){
			vis.swap(righta, rightb);
			righta++;
			rightb++;
		}
		vis.setColor(pivota, 0);
		vis.setColor(pivotb, 0);
		final int afin = leftb,bfin = righta-1;
		vis.setColor(afin, 0xff00ff00);
		vis.setColor(bfin, 0xff00ff00);
		return Sorter.combine(
				r == 0 ? COMPLETED_STAGE : sortTask(vis.subArray(leftb+1,righta-leftb-2),this,srt,exe),
				sortTask(vis.subArray(0, leftb),this,srt,exe),
				sortTask(vis.subArray(righta, vis.size-righta),this,srt,exe),
				() -> {
					vis.setColor(afin, 0);
					vis.setColor(bfin, 0);
				});
	}),
	DLL = new AbstractSibling("Dual pivot Left-Left",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivota = 0,pivotb = vis.size-1;//Absolute
		pickPivot(vis, 0, vis.size, pivota);
		vis.setColor(pivota, 0xffffff00);
		pickPivot(vis, 1,vis.size-1, pivotb);
		int r = vis.compareAndSwap(pivota, pivotb);// a < b
		vis.setColor(pivotb, 0xffffff00);
		int lefta = 1,leftb = 1,right = vis.size-1;
		while(leftb < right){
			switch(vis.compare(leftb, pivota, pivotb)){
			case -1:vis.swap(leftb, lefta);lefta++;leftb++;break;
			case 1:right--;vis.swap(leftb, right);break;
			case 0:leftb++;break;
			}
		}
		lefta--;
		vis.setColor(pivota, 0);
		vis.swap(pivota, lefta);
		vis.setColor(pivotb, 0);
		vis.swap(pivotb, right);
		final int afin = lefta,bfin = right;
		vis.setColor(afin, 0xff00ff00);
		vis.setColor(bfin, 0xff00ff00);
		return Sorter.combine(
				sortTask(vis.subArray(0, lefta),this,srt,exe),
				r == 0 ? COMPLETED_STAGE : sortTask(vis.subArray(lefta+1, right-lefta-1),this,srt,exe),
				sortTask(vis.subArray(right+1, vis.size-right-1),this,srt,exe),
				() -> {
					vis.setColor(afin, 0);
					vis.setColor(bfin, 0);
				});
	}),
	DRR = new AbstractSibling("Dual pivot Right-Right",(VisualArray vis,Sorter srt, Executor exe) -> {
		final int pivota = 0,pivotb = vis.size-1;//Absolute
		pickPivot(vis, 0, vis.size, pivota);
		vis.setColor(pivota, 0xffffff00);
		pickPivot(vis, 1, vis.size-1, pivotb);
		int r = vis.compareAndSwap(pivota, pivotb);// a < b
		vis.setColor(pivotb, 0xffffff00);
		int left = 1,righta = vis.size-2,rightb = righta;
		while(left <= righta){
			switch(vis.compare(righta, pivota, pivotb)){
			case 0:righta--;break;
			case -1:vis.swap(righta, left);left++;break;
			case 1:vis.swap(rightb, righta);rightb--;righta--;
			}
		}
		rightb++;
		left--;
		vis.setColor(pivota, 0);
		vis.swap(pivota, left);
		vis.setColor(pivotb, 0);
		vis.swap(pivotb, rightb);
		final int afin = left,bfin = rightb;
		vis.setColor(left, 0xff00ff00);
		vis.setColor(rightb, 0xff00ff00);
		return Sorter.combine(
				sortTask(vis.subArray(rightb+1, vis.size-rightb-1),this,srt,exe),
				sortTask(vis.subArray(0, left),this,srt,exe),
				r == 0 ? COMPLETED_STAGE : sortTask(vis.subArray(left+1, rightb-left-1),this,srt,exe),
				() -> {
					vis.setColor(afin, 0);
					vis.setColor(bfin, 0);
				});
	});
	public final List<Sorter> subSorters = List.of(SLL,SLR,SRR,TLL,TLR,TRR,DLL,DLR,DRR);
	public Iterator<Sorter> iterator(){return subSorters.iterator();}
	public String toString(){
		return "Quick Sort - Random";
	}
}
