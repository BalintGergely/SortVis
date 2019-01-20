package g.sort.vis;
/**
 * A utility class used to find the <code>n</code>th highest value of a set of randomly ordered <code>long</code> values.
 * <br>The procedure is simple:
 * <br>1. Set the <code>values</code> field to an array with length <code>n</code>.
 * <br>2. Set the <code>size</code> to zero.
 * <br>3. Call <code>put(long)</code> with all of the values.
 * <br>4. <code>first()</code> will return the <code>n</code>th highest value.
 * @author balintgergely
 *
 */
public class LongHeap {
	public long[] values;
	public int size;
	public LongHeap(int count) {
		values = new long[count];
	}
	public long first(){
		return values[0];
	}
	public void put(long value){
		if(size < values.length){
			siftUp(value,size);
			size++;
		}else if(values[0] < value){
			siftDown(value,0);
		}
	}
	private void siftUp(long value,int index){
		while(true){
			int p = (index-1)/2;
			if(values[p] > value){
				values[index] = values[p];
				index = p;
				if(index == 0){
					break;
				}
			}else{
				values[index] = value;
				break;
			}
		}
	}
	private void siftDown(long value,int index){
		while(true){
			int c1 = index*2+1,
				c2 = index*2+2;
			if(c1 >= size){
				values[index] = value;
				break;
			}
			if(c2 < size && values[c2] < values[c1]){
				c1 = c2;
			}
			if(values[c1] < value){
				values[index] = values[c1];
				index = c1;
			}else{
				values[index] = value;
				break;
			}
		}
	}
}
