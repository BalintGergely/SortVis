package g.sort.vis;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.IntUnaryOperator;
/**
 * The visual array is the main unit to be shuffled and sorted.
 * Be sure to always call the right method on this because future implementations may count the number of calls to each method.
 */
public final class VisualArray{
	/**
	 * min: Smallest value in array<br>
	 * max: Largest value in array<br>
	 * range: Difference between min and max. Maximum number of different values<br>
	 * offset: Internally used. Not important<br>
	 * size: The length of this array
	 */
	public final int min,max,range,offset,size;
	private final int[] valuePool;
	private final int[] mainArray;
	private final int[] shanArray;
	private final int[] mainColor;
	private final int[] shanColor;
	private final long[] mainCooldown;
	private final long[] shanCooldown;
	private final long visualCooldown = TimeUnit.MILLISECONDS.toNanos(200);
	public VisualArray(IntUnaryOperator valueSupplier,int length,BiIntBooleanConsumer event) {
		if(length < 3){
			throw new IllegalArgumentException();
		}
		this.offset = 0;
		this.event = event == null ? BiIntBooleanConsumer.EMPTY : event;
		valuePool = new int[length];
		for(int i = 0;i < length;i++){
			if((valuePool[i] = valueSupplier.applyAsInt(i)) < 0){
				throw new IllegalArgumentException();
			}
		}
		size = length;
		mainArray = Arrays.copyOf(valuePool, length);
		Arrays.sort(valuePool);//This one time we actually rely on Java's builtin sorter which is Tim Sort. Not very efficient on memory, but fast.
		min = valuePool[0];
		max = valuePool[length-1];
		range = max-min;
		shanArray = new int[length];
		mainColor = new int[length];
		shanColor = new int[length];
		mainCooldown = new long[length];
		shanCooldown = new long[length];
	}
	private VisualArray(VisualArray cop,int off,int len){
		min = cop.min;
		max = cop.max;
		range = cop.range;
		valuePool = cop.valuePool;
		mainArray = cop.mainArray;
		shanArray = cop.shanArray;
		mainColor = cop.mainColor;
		shanColor = cop.shanColor;
		mainCooldown = cop.mainCooldown;
		shanCooldown = cop.shanCooldown;
		event = cop.event;
		offset = off;
		size = len;
	}
	public void checkRange(int index){
		if(index < 0 || index > size){
			throw new IndexOutOfBoundsException();
		}
	}
	public void checkInterval(int index,int length){
		if(index < 0 || length < 0 || index + length > size){
			throw new IndexOutOfBoundsException();
		}
	}
	/**
	 * Returns the value that is supposed to be at the specified index when the array is sorted.
	 * @param index
	 * @return
	 */
	public int getSortedValue(int index){
		return valuePool[offset+index];
	}
	private int[] array(boolean s){
		return s ? shanArray : mainArray;
	}
	private void setCooldown(boolean s,int i,long cd){
		(s ? shanCooldown : mainCooldown)[offset+i] = cd;
	}
	/**
	 * @param i The index in the main array
	 * @return The cooldown time of the given index from the main array
	 * If this value is higher than <code>System.nanoTime()</code> then the value is on cooldown
	 */
	public long getCooldown(int i){
		checkRange(i);
		return mainCooldown[offset+i];
	}
	/**
	 * @param s Specifies whether or not it's the shandow array
	 * @param i The index in the given array
	 * @return The cooldown time of the given index from the given array
	 * If this value is higher than <code>System.nanoTime()</code> then the value is on cooldown
	 */
	public long getCooldown(boolean s,int i){
		checkRange(i);
		return (s ? shanCooldown : mainCooldown)[offset+i];
	}
	/**
	 * Compares the values at the two indexes in the main array. Fires an event for both of the values being measured.
	 * @param a The first index in the main array
	 * @param b The second index in the main array
	 * @return The result of the comparison using <code>Integer.compare(int,int)</code>
	 */
	public int compare(int a,int b){
		return compare(false,a,false,b);
	}
	/**
	 * Compares the values at the two indexes in the main array. Fires an event for both of the values being measured.
	 * @param as Whether or not the first index refers to the shandow array
	 * @param a The first index in the given array
	 * @param bs Whether or not the second index refers to the shandow array
	 * @param b The second index in the given array
	 * @return The result of the comparison using <code>Integer.compare(int,int)</code>
	 */
	public int compare(boolean as,int a,boolean bs,int b) {
		checkRange(a);
		checkRange(b);
		long cd = System.nanoTime()+visualCooldown;
		setCooldown(as,a,cd);
		setCooldown(bs,b,cd);
		int x = array(as)[offset+a],y = array(bs)[offset+b];
		fireEvent(x,y,false);
		return Integer.compare(x,y);
	}
	/**
	 * Simplifies comparison between three values. Fires an event for both comparisons.
	 * @param a The index in the main array to compare
	 * @param b The first index in the main array to compare to
	 * @param c The second index in the main array to compare to
	 * @return
	 * if m[a] < m[b] then -1<br>
	 * else if m[a] > m[c] then 1<br>
	 * otherwise 0
	 */
	public int compare(int a,int b,int c){
		return compare(false,a,false,b,false,c);
	}
	/**
	 * Simplifies comparison between three values. This method may fire an event for both comparisons.
	 * @param as The array to pick the main element from
	 * @param a The index of the specified array to compare with
	 * @param bs The array to pick the first element from
	 * @param b The index of the specified array for the first element
	 * @param cs The array to pick the second element from
	 * @param c The second index in the main array to compare to
	 * @return
	 * if m[a] < m[b] then -1<br>
	 * else if m[a] > m[c] then 1<br>
	 * otherwise 0
	 */
	public int compare(boolean as,int a,boolean bs,int b,boolean cs,int c){
		checkRange(a);
		checkRange(b);
		checkRange(c);
		long cd = System.nanoTime()+visualCooldown;
		setCooldown(as,a,cd);
		setCooldown(bs,b,cd);
		setCooldown(cs,c,cd);
		int i = array(as)[offset+a],bi = array(bs)[offset+b],ci = array(cs)[offset+c];
		fireEvent(i,bi,false);
		if(i < bi){
			return -1;
		}
		fireEvent(i,ci,false);
		if(i > ci){
			return 1;
		}
		return 0;
	}
	/**
	 * Compares the two indexes in the main array and swaps them if m[a] is larger than m[b]. Fires an event for both values.
	 * @param a The index of the first element
	 * @param b The index of the second element
	 * @return The result of the comparison using <code>Integer.compare(int,int)</code>
	 */
	public int compareAndSwap(int a,int b){
		return compareAndSwap(false,a,false,b);
	}
	/**
	 * Compares the two indexes in the specified arrays and swaps them if as[a] is larger than bs[b]. Fires an event for both values.
	 * @param as The array of the first element
	 * @param a The first index in the given array
	 * @param bs The array of the second element
	 * @param b The second index in the given array
	 * @return The result of the comparison using <code>Integer.compare(int,int)</code>
	 */
	public int compareAndSwap(boolean as,int a,boolean bs,int b){
		if(as == bs && a == b){
			return 0;
		}
		checkRange(a);
		checkRange(b);
		long cd = System.nanoTime()+visualCooldown;
		setCooldown(as,a,cd);
		setCooldown(bs,b,cd);
		a += offset;
		b += offset;
		int[] aa = array(as),bb = array(bs);
		int x = aa[a],y = bb[b];
		int i = Integer.compare(x,y);
		if(i > 0){
			aa[a] = y;
			bb[b] = x;
		}
		fireEvent(x,y,true);
		return i;
	}
	/**
	 * A utility method for value retireval used by edt and sorting algorythms that take advantage of the integer type.
	 * This method doesn't fire any events. <b>Do not compare values returned by this method.</b>
	 * @param i The index in the main array
	 * @return The integer value of the given index in the main array.
	 */
	public int getValue(int i){
		checkRange(i);
		return mainArray[offset+i];
	}
	/**
	 * A utility method for value retireval used by drawing and when sorting algorythms take advantage of the integer type.
	 * This method doesn't fire any events. <b>Do not compare the values returned by this method.</b>
	 * @param s The array to retrieve the value from
	 * @param i The index of the value in the specified array
	 * @return The integer value of the given index from the given array.
	 */
	public int getValue(boolean s,int i){
		checkRange(i);
		return array(s)[offset+i];
	}
	/**
	 * Sets the specified value in the main array to cooldown. Fires an event on the value.
	 * @param i the index of the value
	 */
	public void pingValue(int i){
		checkRange(i);
		setCooldown(false,i,System.nanoTime()+visualCooldown);
		fireEvent(mainArray[offset+i],-1,false);
	}
	/**
	 * Sets the specified value in the specified array to cooldown. Fires an event on the value.
	 * @param s The array
	 * @param i The value
	 */
	public void pingValue(boolean s,int i){
		checkRange(i);
		setCooldown(s,i,System.nanoTime()+visualCooldown);
		fireEvent(array(s)[offset+i],-1,false);
	}
	/**
	 * Sets the specified values in the main array to cooldown. Does not fire any events.
	 * @param is The indexes to update
	 */
	public void pingValues(int...is){
		pingValues(false,is);
	}
	/**
	 * Sets the specified values in the specified array to cooldown. Does not fire any events
	 * @param s The array to update
	 * @param is The indexes to update
	 */
	public void pingValues(boolean s,int...is){
		long cd = System.nanoTime()+visualCooldown;
		for(int i : is){
			if(i >= 0 && i < size)setCooldown(s,i,cd);
		}
	}
	/**
	 * Swaps the two indexes in the main array. Fires an event for both values.
	 * @param a The first index in the main array
	 * @param b The second index in the main array
	 */
	public void swap(int a,int b){
		swap(false,a,false,b);
	}
	/**
	 * Swaps the two indexes in the two specified arrays. Fires an event for both values.
	 * @param as The first array
	 * @param a The index of the first value in the specified array
	 * @param bs The second array
	 * @param b The index of the second value in the specified array
	 */
	public void swap(boolean as,int a,boolean bs,int b){
		if(as == bs && a == b){
			return;
		}
		checkRange(a);
		checkRange(b);
		long cd = System.nanoTime()+visualCooldown;
		setCooldown(as,a,cd);
		setCooldown(bs,b,cd);
		a += offset;
		b += offset;
		int[] aa = array(as),bb = array(bs);
		int x = aa[a],y = bb[b];
		aa[a] = y;
		bb[b] = x;
		fireEvent(x,y,true);
	}
	/**
	 * Increments the value at the specified index in the specified array by one. Does not fire an event.
	 * @param s
	 * @param index
	 * @return The updated value
	 */
	public int incrementAndGet(boolean s,int index){
		checkRange(index);
		setCooldown(s, index, System.nanoTime()+visualCooldown);
		index += offset;
		int v = array(s)[index]+1;
		if(v > max){
			throw new IllegalStateException();
		}
		return array(s)[index] = v;
	}
	/**
	 * Decrements the value at the specified index in the specified array by one. Does not fire an event.
	 * @param s
	 * @param index
	 * @return the updated value
	 */
	public int decrementAndGet(boolean s,int index){
		checkRange(index);
		setCooldown(s, index, System.nanoTime()+visualCooldown);
		index += offset;
		int v = array(s)[index]-1;
		if(v < min){
			throw new IllegalStateException();
		}
		return array(s)[index] = v;
	}
	/**
	 * Copies the value of the first index to the second index in the main array. Fires an event for the value being copied.
	 * @param sp The source index to copy
	 * @param tp The target index to copy to
	 */
	public void copy(int sp,int tp){
		copy(false,sp,false,tp);
	}
	/**
	 * Copies the value of the first index to the second index in the specified arrays. Fires an event for the value being copied.
	 * @param source the source array
	 * @param sp The source index to copy
	 * @param target the target array
	 * @param tp The target index to copy to
	 */
	public void copy(boolean source,int sp,boolean target,int tp){
		checkRange(sp);
		checkRange(tp);
		long cd = System.nanoTime()+visualCooldown;
		setCooldown(source,sp,cd);
		setCooldown(target,tp,cd);
		fireEvent(array(target)[offset+tp] = array(source)[offset+sp],-1,true);
	}
	/**
	 * Copies a sequence of values from the source index to the target index in the main array. Does not fire an event.
	 * @param sp The source index to copy
	 * @param tp The target index to copy to
	 * @param len The length of the sequence.
	 */
	public void copy(int sp,int tp,int len){
		checkInterval(sp,len);
		checkInterval(tp,len);
		System.arraycopy(mainArray, offset+sp, mainArray, offset+tp, len);
	}
	/**
	 * Copies a sequence of values from the source index in the source array to the target index in the target array. Does not fire an event.
	 * @param source The source array to copy from
	 * @param sp The source index to copy
	 * @param target The target array to copy to
	 * @param tp The target index to copy to
	 * @param len The length of the sequence.
	 */
	public void copy(boolean source,int sp,boolean target,int tp,int len){
		checkInterval(sp,len);
		checkInterval(tp,len);
		System.arraycopy(array(source), offset+sp, array(target), offset+tp, len);
	}
	/**
	 * Fills the specified interval in the target index with the value from the source index. Fires an event for the value used for the fill.
	 * @param sp The index of the value to fill with
	 * @param tp The first index of the interval to fill
	 * @param len The length of the interval to fill
	 */
	public void fill(int sp,int tp,int len){
		fill(false,sp,false,tp,len);
	}
	/**
	 * Fills the specified interval in the target index with the value from the source index. Fires an event for the value used for the fill.
	 * @param source The source array to pick the value from
	 * @param sp The index of the value in the source array
	 * @param target The target array to fill
	 * @param tp The first index of the interval to fill
	 * @param len The length of the interval to fill
	 */
	public void fill(boolean source,int sp,boolean target,int tp,int len){
		checkRange(sp);
		checkInterval(tp,len);
		int v = array(source)[offset+sp];
		Arrays.fill(array(target), offset+tp, offset+tp+len, v);
		long cd = System.nanoTime()+visualCooldown;
		Arrays.fill(target ? shanCooldown : mainCooldown, offset+tp, offset+tp+len, cd);
		setCooldown(source,sp,cd);
		fireEvent(v,-1,true);
	}
	public int getColor(int i){
		checkRange(i);
		return mainColor[offset+i];
	}
	public int getColor(boolean s,int i){
		checkRange(i);
		return (s ? shanColor : mainColor)[offset+i];
	}
	public void setColor(int i,int c){
		checkRange(i);
		mainColor[offset+i] = c;
	}
	public void setColor(boolean s,int i,int c){
		checkRange(i);
		(s ? shanColor : mainColor)[offset+i] = c;
	}
	/**
	 * Resets all color and the shandow array.
	 */
	public void clearVisuals(){
		Arrays.fill(shanArray, offset, offset+size, min);
		Arrays.fill(mainColor, offset, offset+size, min);
		Arrays.fill(shanColor, offset, offset+size, min);
	}
	/**
	 * Shuffles the main array using the given random. Assuming that the source of randomness is truly random, every permutation has an equal chance to occur.
	 * @param random The source of randomo.
	 * @param notify Whether or not to fire events for the values being modified.
	 */
	public void shuffle(Random random,boolean notify){
		if(size < mainArray.length){
			throw new UnsupportedOperationException();
		}
		clearVisuals();
		/* To produce each permutation with equal chance, a special algorythm is in place.
		 * Imagine you have a deck of cards. You want to shuffle it in a way that every permutation has an equal chance of occurring. (Source deck)
		 * Define a target deck with 0 cards. You want to move all cards to the target deck. (Target deck)
		 * 
		 * While the source deck is not empty
		 * -Poll a random card out of the source deck. Easy enough.
		 * -Place that card on the top of the target deck.
		 * 
		 * This kinda works like Selection Sort.
		 * 
		 * Because you have a shuffled sequence you don't want to disturb and a not shuffled sequence you want to consume. Except, both are in the same array.
		 */
		int i = size;
		while(i > 1){
			int t = random.nextInt(i);
			i--;
			if(t != i){
				int x = mainArray[i],y = mainArray[t];
				mainArray[i] = y;
				mainArray[t] = x;
				if(notify){
					mainCooldown[i] = mainCooldown[t] = System.nanoTime()+visualCooldown;
					fireEvent(x,y,true);
				}
			}
		}
		fireEvent(-1,-1,true);
	}
	/**
	 * Ya can't stop the MAGÍC!<br>
	 * Ahem... sorts the array using the sorted version as reference with a <i>cool</i> effect.
	 */
	public void cheatSort() {
		if(size < mainArray.length){
			throw new UnsupportedOperationException();
		}
		clearVisuals();
		int stepSize = range >>> 16;
		if(stepSize <= 0){
			stepSize = 1;
		}
		Arrays.fill(shanColor, 0x80808080);
		System.arraycopy(mainArray, 0, shanArray, 0, size);
		boolean b,f;
		do{b = false;f = false;
			int x = -1,y = -1;
			long cd = System.nanoTime()+visualCooldown;
			for(int i = 0;i < size;i++){
				int value = mainArray[i];
				int target = valuePool[i];
				if(value < target){
					value += stepSize;
					if(value > target){
						value = target;
					}
					mainArray[i] = value;
					mainCooldown[i] = cd;
					b = true;
				}else if(value > target){
					value -= stepSize;
					if(value < target){
						value = target;
					}
					mainArray[i] = value;
					mainCooldown[i] = cd;
					b = true;
				}else{
					if(shanColor[i] != 0){
						mainCooldown[i] = cd+visualCooldown;
						if(f){
							y = mainArray[i];
						}else{
							x = mainArray[i];
						}
						f = true;
					}
					shanColor[i] = 0;
				}
			}
			if(f){
				fireEvent(x,y,true);
			}
		}while(b);
	}
	/**
	 * @return true if the content of this visual array is in ascending order.
	 */
	public boolean check() {
		clearVisuals();
		int[] cas = (range >= size) ? new int[range+1] : shanArray;
		for(int v : valuePool){
			cas[v-min]++;
		}
		boolean err = false;
		for(int i = 0;i < size;i++){
			int val = mainArray[i+offset];
			if(val == valuePool[i]){
				cas[val-min]--;
			}else{
				err = true;
			}
		}
		for(int i = 0;i < size;i++){
			int val = mainArray[i+offset];
			if(val == valuePool[i]){
				mainColor[i+offset] = 0xD000ff00;
			}else{
				int x = (--cas[val-min]);
				if(x < min){
					mainColor[i+offset] = 0xD0800000;
				}else{
					mainColor[i+offset] = 0xB0B00000;
				}
			}
			mainCooldown[i+offset] = System.nanoTime()+visualCooldown;
			fireEvent(val,-1,false);
		}
		if(!err){
			Arrays.fill(mainColor, offset, offset+size, 0);
		}
		fireEvent(-1,-1,true);
		return !err;
	}
	/**
	 * @return true if the content of this visual array is in ascending order.
	 */
	public boolean localCheck(){
		int high = offset+size;
		int val = Integer.MIN_VALUE;
		for(int i = offset;i < high;i++){
			int vil = mainArray[i];
			if(vil < val){
				return false;
			}
			val = vil;
		}
		return true;
	}
	private BiIntBooleanConsumer event;
	public void fireEvent(int a,int b,boolean c){
		event.accept(a, b, c);
	}
	/**
	 * Returns a VisualArray representing a sub-section of this array. Changes to the returned array will be reflected in this array and vice-versa.
	 * @param off The index where the returned array should start
	 * @param len The length of the returned array
	 * @return A new visual array starting at <code>off</code> with size <code>length</code>
	 */
	public VisualArray subArray(int off,int len){
		checkInterval(off, len);
		return new VisualArray(this,offset+off,len);
	}
	/**
	 * @return true, if changes made to either <code>this</code> or <code>other</code> may have an effect on both arrays. False otherwise.
	 */
	public boolean overlaps(VisualArray other){
		if(valuePool != other.valuePool){
			return false;
		}
		return (offset < other.offset) ? (offset+size > other.offset) : (other.offset+other.size > offset);
	}
}
