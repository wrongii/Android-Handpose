package ust.csit5110.handpose.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * ImageProcessing provide static algorithms
 * @author Ron
 *
 */
public class ImageProcessing {
	
	// Skin color extraction algorithm
	public static Bitmap ExtractSkinColor(Bitmap sourceImage) {
		float maxH = Float.parseFloat(DataWareHouse.Instance().GetAttribute("MaxH"));
		float minH = Float.parseFloat(DataWareHouse.Instance().GetAttribute("MinH"));
		float maxS = Float.parseFloat(DataWareHouse.Instance().GetAttribute("MaxS"));
		float minS = Float.parseFloat(DataWareHouse.Instance().GetAttribute("MinS"));
		float maxV = Float.parseFloat(DataWareHouse.Instance().GetAttribute("MaxV"));
		float minV = Float.parseFloat(DataWareHouse.Instance().GetAttribute("MinV"));
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		Bitmap bmpSkin = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		int color = 0;
		float[] hsv = {0f, 0f, 0f};
		for(int x=0; x<width; x++){
			for(int y=0; y<height; y++){
				color = sourceImage.getPixel(x, y);				
				Color.colorToHSV(color, hsv);
				if ((hsv[0]<maxH && hsv[0]>minH) && 
						(hsv[1]>minS && hsv[1]<maxS) && 
						(hsv[2]<maxV && hsv[2]>minV)){ // not red to yellow color
					bmpSkin.setPixel(x, y, Color.WHITE);// original color Color.HSVToColor(hsv));
				}
				else
				{
					bmpSkin.setPixel(x, y, Color.BLACK);
				}
			}
		}
		return bmpSkin;
	}

	// Implement the tinting algorithm
	public static Bitmap TintPicture(int deg, Bitmap input) {
		int width = input.getWidth();
		int height = input.getHeight();
		int[] pix = new int[width * height];
		input.getPixels(pix, 0, width, 0, 0, width, height);
		double angle = (3.14159d * (double)deg) / 180.0d;	        
		int S = (int)(256.0d * Math.sin(angle));	        
		int C = (int)(256.0d * Math.cos(angle));
		int r, g, b, index;
		int RY, BY, RYY, GYY, BYY, R, G, B, Y;		
		for (int y = 0; y < height; y++) {	    
			for (int x = 0; x < width; x++) {	    	    	
				index = y * width + x;	    	    	
				r = (pix[index] >> 16) & 0xff;	    
				g = (pix[index] >> 8) & 0xff;	    	    	
				b = pix[index] & 0xff;	    	    	
				RY = (70 * r - 59 * g - 11 * b) / 100;	    	    	
				BY = (-30 * r - 59 * g + 89 * b) / 100;	    	    	
				Y = (30 * r + 59 * g + 11 * b) / 100; 	        	
				RYY = (S * BY + C * RY) / 256;	    	    	
				BYY = (C * BY - S * RY) / 256;	    	    	
				GYY = (-51 * RYY - 19 * BYY) / 100;	    	    	
				R = Y + RYY;	    	    	
				R = (R < 0) ? 0 : ((R > 255) ? 255 : R);	    	    	
				G = Y + GYY;	    	    	
				G = (G < 0) ? 0 : ((G > 255) ? 255 : G);	    	    	
				B = Y + BYY;	    	    	
				B = (B < 0) ? 0 : ((B > 255) ? 255 : B);	   	    	
				pix[index] = 0xff000000 | (R << 16) | (G << 8) | B;   	    		
			}
		}
		Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bm.setPixels(pix, 0, width, 0, 0, width, height); 	
		pix = null;
		return bm;
	}
	
	// Gray scale algorithem
	public static Bitmap GrayScale(Bitmap sourceImage) {
		int width, height;
		width = sourceImage.getWidth();
		height = sourceImage.getHeight();
		Bitmap bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		Canvas c = new Canvas(bmpGray);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(sourceImage, 0, 0, paint);
		return bmpGray;
	}
	
	// Resize picture
	public static Bitmap resizeBitmap(Bitmap bm, float scale){
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap resizedBm = Bitmap.createBitmap(
				bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, false);
		return resizedBm;
	}
}
