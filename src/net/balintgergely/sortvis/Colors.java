package net.balintgergely.sortvis;

import static java.lang.Math.floor;
import static java.lang.Math.sqrt;

/**
 * A utility class for blending colors and converting from HSV to RGB.
 * 
 * @author balintgergely
 *
 */

public class Colors {private Colors() {}
	/**
	 * Blends the two colors as if <code>b</code> was painted over <code>a</code>. This method <b>is different</b> from a usual blending method.
	 */
	public static int specialBlend(int a, int b){
		int bA = (b >> 24) & 0xff;
		if(bA == 0){
			return a;
		}
		int aA = (a >> 24) & 0xff;
		if(aA == 0){
			return b;
		}
		int aR = (a >> 16) & 0xff;
		int aG = (a >> 8) & 0xff;
		int aB = a & 0xff;
		int bR = (b >> 16) & 0xff;
		int bG = (b >> 8) & 0xff;
		int bB = b & 0xff;
		if(aA+bA > 0xff){
			aA = 0xff-bA;
		}
		aR = (int) floor(sqrt(((aR*aR * aA) + (bR*bR * bA)) / (double)(aA + bA)));
		aG = (int) floor(sqrt(((aG*aG * aA) + (bG*bG * bA)) / (double)(aA + bA)));
		aB = (int) floor(sqrt(((aB*aB * aA) + (bB*bB * bA)) / (double)(aA + bA)));
		return (aA + bA) << 24 | aR << 16 | aG << 8 | aB;
	}
	/**
	 * Converts HSV to RGB.
	 * 
	 * @param hu 0 - 360
	 * @param sa 0 - 100
	 * @param br 0 - 100
	 * @param al 0 - 100
	 */
	public static int HSVtoRGB(float hu,float sa,float br,float al) {
		hu = hu/360;
		sa = sa/100f;//Difference between max and min
		br = br*2.55f;//The max of r g b
		al = al*2.55f;
		if(sa < 0){
			sa = -sa;
			hu += 0.5f;
		}
		int r,g,b;
		if(sa != 0){
			hu = (hu - (float)Math.floor(hu)) * 6.0f;
			int fh = (int)Math.floor(hu);
			float
				f = hu - (float)java.lang.Math.floor(hu),
				p = br * (1.0f - sa) + 0.5f,
				md = br * (1.0f - sa * (fh%2 == 0 ? (1.0f - f) : f)) + 0.5f;
			br += 0.5f;
			switch(fh){
			case 0:
	            r = (int)br;
	            g = (int)md;
	            b = (int)p;
	            break;
	        case 1:
	            r = (int)md;
	            g = (int)br;
	            b = (int)p;
	            break;
	        case 2:
	            r = (int)p;
	            g = (int)br;
	            b = (int)md;
	            break;
	        case 3:
	            r = (int)p;
	            g = (int)md;
	            b = (int)br;
	            break;
	        case 4:
	            r = (int)md;
	            g = (int)p;
	            b = (int)br;
	            break;
	        case 5:
	            r = (int)br;
	            g = (int)p;
	            b = (int)md;
	            break;
			default:
				throw new IllegalArgumentException();
			}
		}else{
			r = g = b = (int)(br + 0.5f);
		}
		return ((~((int)al) & 0xff) << 24) | (r << 16) | (g << 8) | b;
	}
}
