package net.balintgergely.sortvis.algos;

import java.util.function.ObjIntConsumer;
/**
 * Used by both ShellSort and CombSort since they both do essentially the same thing with two different sorting algorythms.
 * @author balintgergely
 *
 */
public enum GapSequence{
	SHELL("Shell 1959"){
		@Override
		public <O> void iterate(int n,O obj,ObjIntConsumer<O> cns){
			while(n > 0){
				cns.accept(obj, n);
				n /= 2;
			}
		}
	},
	FRANK_LAZARUS("Frank & Lazarus, 1960"){
		@Override
		public <O> void iterate(int size,O obj,ObjIntConsumer<O> cns){
			int n,k = 2;
			do{
				n = (int)(2*Math.floor(size/(double)(1 << k))) + 1;
				k++;
				cns.accept(obj, n);
			}while(n > 1);
			if(n != 1){
				cns.accept(obj, 1);
			}
		}
	},
	HIBBARD("Hibbard, 1963"){
		@Override
		public <O> void iterate(int n,O obj,ObjIntConsumer<O> cns){
			do{
				n = Integer.highestOneBit(n);
				n = n-1;
				cns.accept(obj, n);
			}while(n != 1);
		}
	},
	PAPERNOV_STASEVICH("Papernov & Stasevich, 1965"){
		@Override
		public <O> void iterate(int n,O obj,ObjIntConsumer<O> cns){
			do{
				n = Integer.highestOneBit(n);
				n = n-1;
				cns.accept(obj, n+2);
			}while(n != 1);
			cns.accept(obj, 1);
		}
	},
	PRATT("Pratt, 1971"){
		@Override
		public <O> void iterate(int n,O obj,ObjIntConsumer<O> cns){
			while(n != 1){
				n--;
				int p = n;
				while(p % 3 == 0){
					p /= 3;
				}
				if(Integer.bitCount(p) != 1){
					continue;
				}
				cns.accept(obj, n);
			}
		}
	},
	PRATT_KNUTH("Pratt, 1971, Knuth, 1973"){
		@Override
		public <O> void iterate(int n,O obj,ObjIntConsumer<O> cns){
			n = (n+2)/3;
			int k = 3;
			while((k-1)/2 <= n){
				k *= 3;
			}
			do{
				k /= 3;
				cns.accept(obj, (k-1)/2);
			}while(k > 3);
		}
	},
	INCERPI_SEDGEWICK("Incerpi & Sedgewick, 1985, Knuth"){// oeis.org/A036569
		private final int[] DATA = {3, 7, 21, 48, 112, 336, 861, 1968, 4592,
			13776, 33936, 86961, 198768, 463792, 1391376, 3402672, 8382192,
			21479367, 49095696, 114556624, 343669872, 852913488, 2085837936};
		@Override
		public <O> void iterate(int size, O obj, ObjIntConsumer<O> cns) {
			int index = 0;
			while(index < DATA.length && DATA[index] < size){
				index++;
			}
			while(index > 0){
				index--;
				cns.accept(obj,DATA[index]);
			}
			cns.accept(obj,1);
		}
	},
	SEDGEWICK("Sedgewick, 1982"){
		@Override
		public <O> void iterate(int size,O obj,ObjIntConsumer<O> cns){
			int k = 1;
			while((1 << k*2) + 3*(1 << k-1) + 1 < size){
				k++;
			}
			do{
				k--;
				cns.accept(obj, (1 << k*2) + 3*(1 << k-1) + 1);
			}while(k > 1);
			cns.accept(obj, 1);
		}
	},
	SF_ONE_POINT_THREE("Shrink factor of 1.3"){
		@Override
		public <O> void iterate(int size, O obj, ObjIntConsumer<O> cns) {
			double s = size/1.3;
			int gap;
			while((gap = (int)s) > 1){
				cns.accept(obj, gap);
				s /= 1.3;
			}
			cns.accept(obj, 1);
		}
	},
	SF_TWO_POINT_TWO("Shrink factor of 2.2"){
		@Override
		public <O> void iterate(int size, O obj, ObjIntConsumer<O> cns) {
			double s = size/2.2;
			int gap;
			while((gap = (int)s) > 1){
				cns.accept(obj, gap);
				s /= 2.2;
			}
			cns.accept(obj, 1);
		}
	};
	public final String name;
	private GapSequence(String nm){
		this.name = nm;
	}
	public abstract <O> void iterate(int size,O obj,ObjIntConsumer<O> cns);
	@Override
	public String toString(){
		return name;
	}
}