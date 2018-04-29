package ust.csit5110.handpose.surf;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.Serializable;

public class IntegralImage implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private float[][] mIntImage;
	private int mWidth = -1;
	private int mHeight = -1;
	
	public float[][] getValues(){
		return mIntImage;
	}
	
	public int getWidth(){
		return mWidth;
	}
	
	public int getHeight(){
		return mHeight;
	}
	
	public float getValue(int column, int row){
		return mIntImage[column][row];
	}
	
	public IntegralImage(Bitmap input){
		mIntImage = new float[input.getWidth()][input.getHeight()];
		mWidth = mIntImage.length;
		mHeight = mIntImage[0].length;
		
		int width = input.getWidth();
		int height = input.getHeight();
		
		float sum, intensity;
		int c;
		for ( int y = 0; y < height; y++ ){
			sum = 0F;
			for ( int x = 0; x < width; x++ ){
				c = input.getPixel(x, y);
//				float intensity = (float)(input.getPixel(x, y));
				intensity = Math.round((0.299D*Color.red(c) + 0.587D*Color.green(c) + 0.114D*Color.blue(c)))/255F;
				sum += intensity;
				if ( y == 0 ){
					mIntImage[x][y] = sum;
				} else {
					mIntImage[x][y] = sum + mIntImage[x][y-1];
				}
			}
		}
	}
	
	public static float BoxIntegral(IntegralImage img, int row, int col, int rows, int cols){
		int height = img.getHeight();
		int width = img.getWidth();
		
		// The subtraction by one for row/col is because row/col is inclusive.
		int r1 = Math.min(row,height) - 1;
		int c1 = Math.min(col,width)  - 1;
		int r2 = Math.min(row + rows,height) - 1;
		int c2 = Math.min(col + cols,width)  - 1;

		float A = (r1 >= 0 && c1 >= 0) ? img.getValue(c1,r1) : 0;
		float B = (r1 >= 0 && c2 >= 0) ? img.getValue(c2,r1) : 0;
		float C = (r2 >= 0 && c1 >= 0) ? img.getValue(c1,r2) : 0;
		float D = (r2 >= 0 && c2 >= 0) ? img.getValue(c2,r2) : 0;

//		System.out.println("height = " + height + ", width = " + width);
//		System.out.println("c1 = " + c1 + ", c2 = " + c2 + ", r1 = " + r1 + ", r2 = " + r2);
//		System.out.println("A = " + A + ", B = " + B + ", C = " + C + ", D = " + D); 
		
		return Math.max(0F,A - B - C + D);
	}
}
