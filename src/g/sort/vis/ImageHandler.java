package g.sort.vis;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.function.IntUnaryOperator;

public class ImageHandler extends DataBuffer implements IntUnaryOperator {
	public static final byte	MODE_ROWS = 0,
								MODE_COLUMNS = 1,
								MODE_PIXEL_ROWS = 2,
								MODE_PIXEL_COLUMNS = 3;
	//private int[] indexes;
	private byte mode;
	private VisualArray vis;
	public final int width,height;
	public final BufferedImage image;
	public final BufferedImage altImage;
	public static ImageHandler newInstance(BufferedImage image,byte mode){
		return new ImageHandler(image,image.getWidth(),image.getHeight(),mode);
	}
	private ImageHandler(BufferedImage image,int w,int h,byte mode) {
		super(TYPE_INT, w*h, w*h, w*h);
		this.width = w;
		this.height = h;
		this.mode = mode;
		this.image = image;
		for(int x = 0;x < w;x++){
			for(int y = 0;y < h;y++){
				setRGB(x,y,image.getRGB(x, y));
			}
		}
		switch(this.mode = mode){
		case MODE_ROWS:
			this.size = h;
			/*indexes = new int[h];
			int phash = 0;
			int dex = 0;
			for(int y = 0;y < h;y++){
				int hash = 0;
				for(int x = 0;x < w;x++){
					hash = hash^getRGB(x, y);
				}
				if(y > 0 && phash == hash){
					indexes[y] = dex;
				}else{
					indexes[y] = dex = y;
				}
			}*/
			break;
		case MODE_COLUMNS:
			this.size = w;
			/*indexes = new int[w];
			phash = 0;
			dex = 0;
			for(int x = 0;x < w;x++){
				int hash = 0;
				for(int y = 0;y < h;y++){
					hash = hash^getRGB(x, y);
				}
				if(x > 0 && phash == hash){
					indexes[x] = dex;
				}else{
					indexes[x] = dex = x;
				}
			}*/
			break;
		case MODE_PIXEL_ROWS:
			this.size = w*h;
			/*indexes = new int[w*h];
			phash = 0;
			dex = 0;
			int act = 0;
			for(int x = 0;x < w;x++){
				for(int y = 0;y < h;y++){
					int hash = getRGB(x, y);
					if(x > 0 && phash == hash){
						indexes[act] = dex;
					}else{
						indexes[act] = dex = act;
					}
					act++;
				}
			}*/
			break;
		case MODE_PIXEL_COLUMNS:
			this.size = w*h;
			/*indexes = new int[w*h];
			phash = 0;
			dex = 0;
			act = 0;
			for(int y = 0;y < h;y++){
				for(int x = 0;x < w;x++){
					int hash = getRGB(x, y);
					if(x > 0 && phash == hash){
						indexes[act] = dex;
					}else{
						indexes[act] = dex = act;
					}
					act++;
				}
			}*/
			break;
		default:
			throw new IllegalArgumentException();
		}
		ColorModel model = ColorModel.getRGBdefault();
		altImage = new BufferedImage(
				model,
				WritableRaster.createWritableRaster(
					model.createCompatibleSampleModel(width, height),
					this,
					new Point(0,0)),
				false,
				new Hashtable<>());
	}
	private void setRGB(int x,int y,int rgb){
		if(x >= width || x < 0){
			throw new ArrayIndexOutOfBoundsException();
		}
		offsets[x+width*y] = rgb;
	}
	/*private int getRGB(int x,int y){
		if(x >= width || x < 0){
			throw new ArrayIndexOutOfBoundsException();
		}
		return offsets[y*width+x];
	}*/
	public synchronized VisualArray makeArray(BiIntBooleanConsumer event){
		return vis = new VisualArray(this, this.size, event);
	}
	@Override
	public int applyAsInt(int operand) {
		return operand;//indexes[operand];
	}
	@Override
	public int getElem(int bank, int i) {
		if(vis != null){
			switch(mode){
			case MODE_ROWS:
				i = i%width+(vis.getValue(i/width))*width;
				break;
			case MODE_COLUMNS:
				i = i-(i%width)+(vis.getValue(i%width));
				break;
			case MODE_PIXEL_ROWS:
				i = vis.getValue(i);
				break;
			case MODE_PIXEL_COLUMNS:
				i = vis.getValue(i%width*height + (i/width));
				i = i%height*width + i/height;
				break;
			}
		}
		return offsets[i];
	}
	@Override
	public void setElem(int bank, int i, int val) {
		throw new UnsupportedOperationException();
	}
}
