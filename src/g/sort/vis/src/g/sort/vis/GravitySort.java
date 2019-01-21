package g.sort.vis;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

public class GravitySort extends ConfigurableSorter{
	public GravitySort() {}
	@Override
	public CompletionStage<?> sort(VisualArray vis, Sorter srt, Executor exe) {
		vis.copy(false, 0, true, 0, vis.size);
		for(int i = 0;i < vis.size;i++){
			vis.setColor(true, i, 0x50808080);
		}
		int h = vis.max;
		while(h > vis.min){
			int c = 0;
			for(int i = 0;i < vis.size;i++){
				if(vis.getValue(true, i) >= h){
					vis.fireEvent(vis.decrementAndGet(false, i), -1, false);//Remove a bead
					c++;
				}
			}
			int x = vis.size;
			while(c > 0){
				x--;
				vis.fireEvent(vis.incrementAndGet(false, x), -1, false);
				c--;
			}
			h--;
			if(x != vis.size)vis.fireEvent(h, -1, true);
		}
		vis.clearVisuals();
		return COMPLETED_STAGE;
	}
	public String toString(){
		return "Gravity Sort";
	}
}
