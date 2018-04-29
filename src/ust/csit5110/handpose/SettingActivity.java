package ust.csit5110.handpose;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;

import ust.csit5110.handpose.edge.BinarySobelEdgeDetector;
import ust.csit5110.handpose.edge.CannyEdgeDetector;
import ust.csit5110.handpose.surf.SURFInterestPoint;
import ust.csit5110.handpose.surf.Surf;
import ust.csit5110.handpose.util.DataWareHouse;
import ust.csit5110.handpose.util.ImageProcessing;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingActivity extends Activity {
	// temporary storage for application file list
	private String[] mFileList;
	// application supported file type
	final String FTYPE = (String) DataWareHouse.Instance().GetAttribute("AppFileExtension");
	// static
	private static final String TAG = "";
	private static final int DIALOG_LOAD_FILE = -31788;
	private static final int DIALOG_COMPARE_FILE = -31789;
	// activity members
	private String mBitmapFile; // store the bitmap currently showing on the screen
	private Bitmap mBitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		
		// initiate Open button event
		Button openButton = (Button) findViewById(R.id.btnOpen);
		openButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadFileList();
				popFileDialog();
			}
		});
		
		// initiate Set Template button event
		Button setButton = (Button) findViewById(R.id.btnSetTemplate);
		setButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				updateBitmapToTemplate();
			}
		});
		
		// initiate Compare button event
		Button compareButton = (Button) findViewById(R.id.btnCompare);
		compareButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadFileList();
				popCompareDialog();
			}
		});		
		// initiate effect list update
		Spinner spinner = (Spinner) findViewById(R.id.selectEffect);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				String item = 
				(String) parent.getItemAtPosition(pos);
				applyeffect(item);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Log.d("Spinner.onNothingSelected", "Nothing is selected");
			}
		});
		
		// get the bitmap from datawarehouse
		updateBitmap(DataWareHouse.Instance().GetTemplateBitmap());
		updateBitmapToTemplate();
	}

	@Override
	protected Dialog onCreateDialog(int id){
	    Dialog dialog = null;
	    AlertDialog.Builder builder = new Builder(this);

	    switch(id) {
	        case DIALOG_LOAD_FILE: // customized for file open dialog
	            builder.setTitle("Choose your image (jpg)");
	            if(mFileList == null) {
	                dialog = builder.create();
	                return dialog;
	            }
	            builder.setItems(mFileList, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	mBitmapFile = mFileList[which];
	                	updateBitmap(BitmapFactory.decodeFile(
	                			DataWareHouse.Instance().GetAttribute("AppDirectory") + 
	                			mBitmapFile));
	                	resetEffect();
	                }
	            });
	            break;
	        case DIALOG_COMPARE_FILE:
	            builder.setTitle("Choose your image (jpg)");
	            if(mFileList == null) {
	                dialog = builder.create();
	                return dialog;
	            }
	            builder.setItems(mFileList, new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface dialog, int which) {
	                	mBitmapFile = mFileList[which];
	                	Bitmap tmpBm = BitmapFactory.decodeFile(
	                			DataWareHouse.Instance().GetAttribute("AppDirectory") + 
	                			mBitmapFile);
	                	int matchedCount = Surf.DoSurfCompare(tmpBm, 
	                			DataWareHouse.Instance().GetTemplate());
	                	printToast("Compare Result: " + matchedCount + " found out of " +
	                			DataWareHouse.Instance().GetTemplatePointCount() + " in template");
	                }
	            });
	            break;
	    }
	    dialog = builder.show();
	    return dialog;
	}
	
	/**
	 * print message by Toast
	 */
	private void printToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * reset the effect list to zero position
	 */
	private void resetEffect(){
		Spinner spinner = (Spinner) findViewById(R.id.selectEffect);
		spinner.setSelection(0);
	}
	
	/**
	 * apply image process effect
	 * @param effectName, string name of the effect selected
	 */
	private void applyeffect(String effectName){
		Resources res = getResources();
		if(effectName.equalsIgnoreCase(
				res.getString(R.string.effect_canny))){
			CannyEdgeDetector canny = new CannyEdgeDetector();
			canny.setSourceImage(mBitmap);
			canny.setLowThreshold(0.5f);
			canny.setHighThreshold(1f);
			canny.process();
			updateBitmap(canny.getEdgesImage());
		} else if(effectName.equalsIgnoreCase(
				res.getString(R.string.effect_extract))){
			updateBitmap(ImageProcessing.ExtractSkinColor(mBitmap));
		} else if(effectName.equalsIgnoreCase(
				res.getString(R.string.effect_gray))){
			updateBitmap(ImageProcessing.GrayScale(mBitmap));
		} else if(effectName.equalsIgnoreCase(
				res.getString(R.string.effect_sobel))){
			updateBitmap(BinarySobelEdgeDetector.Detect(mBitmap, 1f));
		} else if(effectName.equalsIgnoreCase(
				res.getString(R.string.effect_tint))){
			updateBitmap(ImageProcessing.TintPicture(60, mBitmap));
		} else if(effectName.equalsIgnoreCase(
				res.getString(R.string.effect_resize))){
			float ratio = Float.parseFloat(DataWareHouse.Instance().GetAttribute("TemplateWidth"))
					/ (float) mBitmap.getWidth();
			updateBitmap(ImageProcessing.resizeBitmap(mBitmap, ratio));
		} else if(effectName.equalsIgnoreCase(
				res.getString(R.string.effect_magnify))){
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			float ratio = (float)size.x / (float) mBitmap.getWidth();
			ImageView iv = (ImageView) findViewById(R.id.imageView1);
			iv.setImageBitmap(ImageProcessing.resizeBitmap(mBitmap, ratio));
			iv.invalidate();
		}
	}
	
	/**
	 * update the bitmap to front end
	 */
	private void updateBitmap(Bitmap newBm){
		mBitmap = newBm;
		ImageView iv = (ImageView) findViewById(R.id.imageView1);
		iv.setImageBitmap(mBitmap);
		iv.invalidate();
	}
	
	/**
	 * set the current bitmap as the application surf template
	 */
	private void updateBitmapToTemplate() {
		DataWareHouse.Instance().SetTemplate(mBitmap);
		List<SURFInterestPoint> points = DataWareHouse.Instance().
				GetTemplate().getFreeOrientedInterestPoints();
		Toast.makeText(this, "updated surf template" + points.size() + 
				" interest points", Toast.LENGTH_SHORT).show();
		SURFInterestPoint pt;
		for(int i=0;i<points.size();i++){
			pt = points.get(i);
			mBitmap.setPixel((int)pt.getX(), (int)pt.getY(), Color.YELLOW);
		}
	}
	
	/**
	 * provide function address for element event call back
	 */
	@SuppressWarnings("deprecation")
	private void popFileDialog() {
		this.showDialog(DIALOG_LOAD_FILE);
	}

	/**
	 * provide function address for element event call back
	 */
	@SuppressWarnings("deprecation")
	private void popCompareDialog() {
		this.showDialog(DIALOG_COMPARE_FILE);
	}	
	
	/**
	 * load file list from application 
	 */
	private void loadFileList() {
		File path =new File( DataWareHouse.Instance().GetAttribute("AppDirectory"));
	    try {
	        path.mkdirs();
	    }
	    catch(SecurityException e) {
	        Log.e(TAG, "unable to write on the sd card " + e.toString());
	    }
	    if(path.exists()) {
	        FilenameFilter filter = new FilenameFilter() {
	        	// filter file by supported extension
	            public boolean accept(File dir, String filename) {
	                File sel = new File(dir, filename);
	                return filename.contains(FTYPE) || sel.isDirectory();
	            }
	        };
	        mFileList = path.list(filter);
	    }
	    else {
	        mFileList= new String[0];
	    }
	}


		
}
