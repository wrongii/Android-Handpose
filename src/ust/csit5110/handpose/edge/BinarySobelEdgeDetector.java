package ust.csit5110.handpose.edge;

import android.graphics.Bitmap;
import android.graphics.Color;

public class BinarySobelEdgeDetector {
	// statics
	private final static float[] SOBEL_HORIZONTAL = {-1f,-2f,-1f,    0f,0f,0f,     1f, 2f,1f};
	private final static float[] SOBEL_VERTICAL   = {-1f, 0f, 1f,   -2f,0f,2f,    -1f, 0f,1f};
	private final static float[] SOBEL_45DIAGONAL = { 0f, 1f, 2f,   -1f,0f,1f,    -2f,-1f,0f};
	private final static float[] SOBEL_135DIAGONAL= {-2f,-1f, 0f,   -1f,0f,1f,     0f, 1f,2f};
	
	/**
	 * Function Detect, 2 direction of edges
	 * @param input a Bitmap object to do edge detection
	 * @param threshold set to 1 if it is a black & white picture
	 * @return a black & white Bitmap object with edges only
	 */
	public static Bitmap Detect(Bitmap input, float threshold){
		int height = input.getHeight();
		int width  = input.getWidth();
		int[] xbin = new int[width];
		int[] ybin = new int[height];
		float filteredHorizontal, filteredVertical;
		int x, y, k, l;
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		for(x=0; x<width-2; x++){
			for(y=0; y<height-2; y++){
				filteredHorizontal = 0f;
				filteredVertical = 0f;
				// horizontal
				for(k=0; k<3; k++){
					for(l=0; l<3; l++){
						filteredHorizontal += (Color.WHITE==input.getPixel(x+k, y+l)?1f:0f)
								* SOBEL_HORIZONTAL[k*3+l];
						filteredVertical += (Color.WHITE==input.getPixel(x+k, y+l)?1f:0f)
								* SOBEL_VERTICAL[k*3+l];
					}
				}
				if(filteredHorizontal>=threshold || filteredVertical>=threshold) {
					result.setPixel(x+1, y+1, Color.WHITE);
					xbin[x+1]++;
					ybin[y+1]++;
				}
			}
		}
		return result;
	}
	
	/**
	 * Function DetectWithDiagonal, 4 direction of edges
	 * @param input a Bitmap object to do edge detection
	 * @param threshold set to 1 if it is a black & white picture
	 * @return a black & white Bitmap object with edges only
	 */
	public static Bitmap DetectWithDiagonal(Bitmap input, float threshold){
		int height = input.getHeight();
		int width  = input.getWidth();
		int[] xbin = new int[width];
		int[] ybin = new int[height];
		float filteredHorizontal, filteredVertical, filtered45, filtered135;
		int x, y, k, l;
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		for(x=0; x<width-2; x++){
			for(y=0; y<height-2; y++){
				filteredHorizontal = 0f;
				filteredVertical = 0f;
				filtered45 = 0f;
				filtered135 = 0f;
				// horizontal
				for(k=0; k<3; k++){
					for(l=0; l<3; l++){
						filteredHorizontal += (Color.WHITE==input.getPixel(x+k, y+l)?1f:0f)
								* SOBEL_HORIZONTAL[k*3+l];
						filteredVertical += (Color.WHITE==input.getPixel(x+k, y+l)?1f:0f)
								* SOBEL_VERTICAL[k*3+l];
						filtered45 += (Color.WHITE==input.getPixel(x+k, y+l)?1f:0f)
								* SOBEL_45DIAGONAL[k*3+l];
						filtered135 += (Color.WHITE==input.getPixel(x+k, y+l)?1f:0f)
								* SOBEL_135DIAGONAL[k*3+l];
					}
				}
				if(filteredHorizontal>=threshold || filteredVertical>=threshold
						|| filtered45>=threshold || filtered135>=threshold) {
					result.setPixel(x+1, y+1, Color.WHITE);
					xbin[x+1]++;
					ybin[y+1]++;
				}
			}
		}
		return result;
	}
}