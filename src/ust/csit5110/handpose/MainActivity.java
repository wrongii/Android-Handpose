package ust.csit5110.handpose;

import ust.csit5110.handpose.util.DataWareHouse;
import ust.csit5110.handpose.util.ImageProcessing;

import java.io.File;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize();
		setContentView(R.layout.activity_main);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	public void btnSetting_click (View view){
		Intent myIntent = new Intent (this, SettingActivity.class);
		startActivity(myIntent);
		
	}
	
	public void btnStart_click (View view){
		Intent myIntent = new Intent (this, TimerActivity.class);
		startActivity(myIntent);
		
	}
	
	/**
	 * Put android context resources into static warehouse
	 */
	private void initialize() {
		Resources res = getResources();
		// application directory = "AppDirectory"
		String appDirectory = Environment.getExternalStorageDirectory() 
				+ res.getString(R.string.app_directory);
		File fileDirectory = new File(appDirectory);
	    try {
	    	fileDirectory.mkdirs();
	    }
	    catch(SecurityException e) {
	        Log.e("MainActivity.initialize", "unable create application directory:" 
	        		+ e.toString());
	    }
	    DataWareHouse house = DataWareHouse.Instance();
		house.SetAttribute("AppDirectory", appDirectory);
		// application file type = "AppFileExtension"
		String appExt = res.getString(R.string.app_file_extension);
		DataWareHouse.Instance().SetAttribute("AppFileExtension", appExt);
		// application setting for skin color range
		DataWareHouse.Instance().SetAttribute("MaxH", res.getString(R.string.setting_max_h));
		DataWareHouse.Instance().SetAttribute("MinH", res.getString(R.string.setting_min_h));
		DataWareHouse.Instance().SetAttribute("MaxS", res.getString(R.string.setting_max_s));
		DataWareHouse.Instance().SetAttribute("MinS", res.getString(R.string.setting_min_s));
		DataWareHouse.Instance().SetAttribute("MaxV", res.getString(R.string.setting_max_v));
		DataWareHouse.Instance().SetAttribute("MinV", res.getString(R.string.setting_min_v));
		// template generation related
		DataWareHouse.Instance().SetAttribute("TemplateWidth", res.getString(R.string.template_width));
		DataWareHouse.Instance().SetAttribute("CompareWidth", res.getString(R.string.compare_width));
		// auto picture taking counter
		DataWareHouse.Instance().SetAttribute("CountTime", res.getString(R.string.setting_counttime));
		DataWareHouse.Instance().SetAttribute("CountTick", res.getString(R.string.setting_counttick));
		// matching criteria
		DataWareHouse.Instance().SetAttribute("MatchCriteria", res.getString(R.string.setting_matchpoint));
		// default Surf template
		Bitmap tmpBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.f2_template);
		float ratio = Float.parseFloat(DataWareHouse.Instance().GetAttribute("TemplateWidth")) 
				/ tmpBitmap.getWidth();
		tmpBitmap = ImageProcessing.resizeBitmap(tmpBitmap, ratio);
		tmpBitmap = ImageProcessing.ExtractSkinColor(tmpBitmap);
		DataWareHouse.Instance().SetTemplate(tmpBitmap);
	}

}
