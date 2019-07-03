package g.sort.vis;

import java.util.Random;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

public class RandomSort extends ConfigurableSorter {
	public RandomSort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		int z = vis.size*(vis.size-1)/2;
		Random rng = ThreadLocalRandom.current();
		do{
			for(int i = 0;i < z;i++){
				int a = rng.nextInt(vis.size-1);
				int b = rng.nextInt(vis.size);
				if(a >= b){
					int c = a;
					a = b;
					b = c+1;
				}
				vis.compareAndSwap(a, b);
			}
		}while(!vis.check());
		return CompletionTask.COMPLETED_STAGE;
	}
	public String toString(){
		return "RandomSort";
	}
}
