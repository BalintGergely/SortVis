package g.sort.vis;

import java.util.Random;

public enum ValueSetGenerator {
	ASCENDING(){
		public int apply(int left, int right,Random rng) {
			return left;
		}
	},
	RANDOM_VALUES(){
		@Override
		public int apply(int index, int size, Random random) {
			return random.nextInt(size);
		}
	},
	N_MINUS_2_EQUAL(){
		@Override
		public int apply(int index, int size, Random random) {
			if(index == 0){
				return 0;
			}
			if(index+1 == size){
				return size;
			}
			return size/2;
		}
	},
	COS(){
		@Override
		public int apply(int index, int size, Random random) {
			int val = (int) Math.round((1-Math.cos(index*Math.PI/size))*size);
			return val < 0 ? size : val;
		}
	},
	SIN(){
		@Override
		public int apply(int index, int size, Random random) {
			int val = (int) Math.round((1-Math.sin(index*Math.PI/size))*size);
			return val < 0 ? size : val;
		}
	},
	COSINUS(){
		@Override
		public int apply(int index, int size, Random random) {
			int val = (int) Math.round((Math.cos(index*Math.PI*2/size)+1)*size);
			return val < 0 ? size : val;
		}
	},
	CUBIC(){
		@Override
		public int apply(int index, int size, Random random) {
			size /= 2;
			index -= size;
			float fff = index/(float)size;
			int val = (int) Math.round(fff*fff*fff*size+size);
			return val < 0 ? 0 : val;
		}
	},
	SMALL_DIFFERENCES(){
		public int apply(int index,int size,Random random) {
			int div = Integer.numberOfLeadingZeros(Integer.highestOneBit(size));
			return index%div;
		}
	};
	public abstract int apply(int index,int size,Random random);
}
