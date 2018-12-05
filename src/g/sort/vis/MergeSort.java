package g.sort.vis;

public class MergeSort extends ConfigurableSorter{
	public MergeSort(){}
	@Override
	public void sort(VisualArray vis,Sorter srt) {
		if(lowLevelFirst){
			int i = Integer.numberOfTrailingZeros(Integer.highestOneBit(vis.size))-1;
			while(i >= 0){
				sort(vis,this,0,vis.size,i);
				i--;
			}
		}else{
			sort(vis,srt,0,vis.size,-1);
		}
	}
	public void sort(VisualArray vis,Sorter srt,int off,int len,int depth){
		switch(len){
		case 2:vis.compareAndSwap(off, off+1);
		case 1:
		case 0:return;
		}
		int left = len/2,right = len-left;
		if(depth != 0){
			if(srt.getClass() == MergeSort.class){
				MergeSort s = (MergeSort)srt;
				s.sort(vis,this,off,left,depth-1);
				s.sort(vis,this,off+left,right,depth-1);
			}else{
				srt.sort(vis.subArray(off, left),vis.subArray(off+left, right),this);
			}
		}
		if(depth <= 0){
			if(variant.equals(IN_PLACE)){
				mergeInPlace(vis,off,left,right);
			}else{
				mergeOutOfPlace(vis,off,left,right,variant.equalsIgnoreCase(OUT_OF_PLACE_OPT));
			}
		}
	}
	public void mergeOutOfPlace(VisualArray vis,int off,int left,int right,boolean op){
		int len = left+right;
		int x = 0,y = 0,z = 0,k = 0;
		while(z < len){//xv = off+x, yv = off+y, zv = off+z
			if(x != left && (y == right || vis.compare(off+x, off+left+y) <= 0)){
				vis.setColor(true,off+z,0x80808080);
				vis.copy(false,off+x,true,off+z);
				if(op){
					vis.setColor(true,off+k,0);
					vis.copy(true,off+k,false,off+k);
					k++;
				}
				x++;
				if(op && x == left){
					len = left+y;
				}
			}else{
				vis.setColor(true,off+z,0x80808080);
				vis.copy(false,off+left+y,true,off+z);
				y++;
			}
			z++;
		}
		while(k < len){
			vis.setColor(true, off+k, 0);
			vis.copy(true, off+k, false, off+k);
			k++;
		}
	}
	public void mergeInPlace(VisualArray vis,int off,int left,int right){
		while(true){
			if(vis.compare(off, off+left) <= 0){
				off++;
				left--;
				if(left == 0){
					return;
				}
			}else{
				vis.copy(false, off+left, true, off);
				vis.copy(off, off+1, left);
				vis.copy(true, off, false, off);
				off++;
				right--;
				if(right == 0){
					return;
				}
			}
		}
	}
	private boolean lowLevelFirst = false;
	private String variant = OUT_OF_PLACE;
	private static final String	OUT_OF_PLACE = "Out of place",
								OUT_OF_PLACE_OPT = "Out of place optimised",
								IN_PLACE = "In place";
	public int getNumberOptions(){return 2;};
	public String getOptionName(int index){
		switch(index){
		case 0:return "Low level first";
		case 1:return "Variant";
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
		case 1:return new String[]{OUT_OF_PLACE,OUT_OF_PLACE_OPT,IN_PLACE};
		}
		return null;
	}
	public void setOption(int index,Object opt){
		switch(index){
		case 0:lowLevelFirst = ((Boolean)opt).booleanValue();break;
		case 1:variant = (String)opt;
		}
	};
	public Object getOption(int index){
		switch(index){
		case 0:return Boolean.valueOf(lowLevelFirst);
		case 1:return variant;
		}
		return null;
	}
	public String toString(){
		return "Merge Sort";
	}
}
