package g.sort.vis;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

import static g.sort.vis.CompletionTask.*;

public class MergeSort extends ConfigurableSorter{
	public MergeSort(){}
	@Override
	public CompletionStage<?> sort(VisualArray vis,Sorter srt, Executor exe) {
		if(lowLevelFirst){
			int i = Integer.numberOfTrailingZeros(Integer.highestOneBit(vis.size))-1;
			if(exe == null){
				while(i >= 0){
					sort(vis,this,null,i);
					i--;
				}
			}else if(i >= 0){
				CompletionStage<?> cml = sort(vis,this,exe,i);
				i--;
				while(i >= 0){
					int zi = i;
					cml = cml.thenRun(() -> sort(vis,this,exe,zi));
					i--;
				}
				return cml;
			}
			return COMPLETED_STAGE;
		}else{
			return sort(vis,srt,exe,-1);
		}
	}
	public CompletionStage<?> sort(VisualArray vis,Sorter srt,Executor exe,int depth){
		if(Sorter.guardedSort(vis)){
			return COMPLETED_STAGE;
		}
		final int left = vis.size/2,right = vis.size-left;
		if(exe == null){
			if(depth != 0){
				if(srt.getClass() == MergeSort.class){
					MergeSort s = (MergeSort)srt;
					s.sort(vis.subArray(0, left),this,null,depth-1);
					s.sort(vis.subArray(left, right),this,null,depth-1);
				}else{
					srt.sort(vis.subArray(0, left),this,null);
					srt.sort(vis.subArray(left, right),this,null);
				}
			}
			if(depth <= 0){
				if(variant.equals(IN_PLACE)){
					mergeInPlace(vis,left,right);
				}else{
					mergeOutOfPlace(vis,left,right,variant.equalsIgnoreCase(OUT_OF_PLACE_OPT));
				}
			}
			return COMPLETED_STAGE;
		}else{
			if(depth != 0){
				CompletionStage<?> a;
				CompletionStage<?> b;
				if(srt.getClass() == MergeSort.class){
					MergeSort s = (MergeSort)srt;
					a = decompose(supplyAsync(() -> s.sort(vis.subArray(0, left),this,exe,depth-1),exe));
					b = decompose(supplyAsync(() -> s.sort(vis.subArray(left, right),this,exe,depth-1),exe));
				}else{
					a = decompose(supplyAsync(() -> srt.sort(vis.subArray(0, left),this,exe),exe));
					b = decompose(supplyAsync(() -> srt.sort(vis.subArray(left, right),this,exe),exe));
				}
				if(depth < 0){
					return a.runAfterBoth(b, () -> {
						if(variant.equals(IN_PLACE)){
							mergeInPlace(vis,left,right);
						}else{
							mergeOutOfPlace(vis,left,right,variant.equalsIgnoreCase(OUT_OF_PLACE_OPT));
						}
					});
				}else{
					return combine(a,b,null);
				}
			}else{
				if(variant.equals(IN_PLACE)){
					mergeInPlace(vis,left,right);
				}else{
					mergeOutOfPlace(vis,left,right,variant.equalsIgnoreCase(OUT_OF_PLACE_OPT));
				}
				return COMPLETED_STAGE;
			}
		}
	}
	public void mergeOutOfPlace(VisualArray vis,int left,int right,boolean op){
		int len = vis.size;
		int x = 0,y = 0,z = 0,k = 0;
		while(z < len){//xv = off+x, yv = off+y, zv = off+z
			if(x != left && (y == right || vis.compare(x, left+y) <= 0)){
				vis.setColor(true,z,0x80808080);
				vis.copy(false,x,true,z);
				if(op){
					vis.setColor(true,k,0);
					vis.copy(true,k,false,k);
					k++;
				}
				x++;
				if(op && x == left){
					len = left+y;
				}
			}else{
				vis.setColor(true,z,0x80808080);
				vis.copy(false,left+y,true,z);
				y++;
			}
			z++;
		}
		while(k < len){
			vis.setColor(true, k, 0);
			vis.copy(true, k, false, k);
			k++;
		}
	}
	public void mergeInPlace(VisualArray vis,int middle,int right){
		int left = 0;
		while(true){
			if(vis.compare(left, left+middle) <= 0){
				left++;
				middle--;
				if(middle == 0){
					return;
				}
			}else{
				vis.copy(false, left+middle, true, left);
				vis.copy(left, left+1, middle);
				vis.copy(true, left, false, left);
				left++;
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
