package ust.csit5110.handpose;

import ust.csit5110.handpose.surf.Surf;
import ust.csit5110.handpose.util.DataWareHouse;
import ust.csit5110.handpose.thread.DelayedSurface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class TimerActivity extends Activity implements DelayedSurface.CameraContext{

	// static
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	
	// private static, for state machine
	private static final int COUNT_DOWN_STATE = 0;
	private static final int COUNT_DOWN_ANALYSING_STATE = 1;
	private static final int READY_TO_SHOT_STATE = 2;
	private static final int ANALYSING_STATE = 3;
	
	// instance members
    private Camera mCamera;
    private DelayedSurface mPreview;
    private String VIEW_LOG_TAG = "";
    private TextView countdown_text;
    private TextView result_text;
    private volatile int mMatchingState = COUNT_DOWN_STATE;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (!checkCameraHardware(this)){
        	return;
        }
        
        setContentView(R.layout.activity_timer);
        countdown_text = (TextView) findViewById(R.id.textCountDown);
        result_text = (TextView) findViewById(R.id.textResultDown);
		
        // initiate the state
        mMatchingState = COUNT_DOWN_STATE;
        printResult("");
        
        // Create an instance of Camera
        Camera tmpCamera = getCameraInstance();
        tmpCamera.setDisplayOrientation(90);

        // Create our Preview view and set it as the content of our activity.
        if(tmpCamera !=null){
        	mCamera = tmpCamera;
        	mPreview = new DelayedSurface(this);
        	mPreview.SetCameraContext(this);
        	initiateCameraMinimumResolution();
        	FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        	preview.addView(mPreview);
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        
        // initiate the state
        mMatchingState = COUNT_DOWN_STATE;
        printResult("");

        // Create an instance of Camera
        Camera tmpCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        if(tmpCamera !=null){
        	mCamera = tmpCamera;
        	mPreview = new DelayedSurface(this);
        	mPreview.SetCameraContext(this);
        	FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        	preview.addView(mPreview);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera(); // release the camera immediately on pause event
    }

    @Override
    public void TakePicture(){
    	if(onTakePicture()){
        	printMessage("Analyzing pictures.. ");
        	mCamera.takePicture(null, null, mMatchPicture);
        	mCamera.startPreview();
    	}
    }
    
    @Override
    public void LastCounterAction(){
    	printMessage("Counter stopped, press button");
    	mMatchingState = READY_TO_SHOT_STATE;
    }
    
    @Override
    public void BindCameraPreview(){
    	try {
			mCamera.setPreviewDisplay(mPreview.GetSurfaceHolder());
			mCamera.startPreview();
		} catch (IOException e) {
			Log.e("TimerActivity::OnCreate", e.getMessage());
		}  
    }
    
    /**
     * Button event to force matching
     */
    public void btnStartCountDown_click (View view){
    	TakePicture();
    }
    
    /**
     * function pointer for matching picture 
     */
    private PictureCallback mMatchPicture = new PictureCallback() {
    	public void onPictureTaken(byte[] data, Camera camera) {
    		printMessage("Picture taken, starting Surf");
        	Bitmap tmpBm = BitmapFactory.decodeByteArray(data, 0, data.length);
        	Matrix matrix = new Matrix();
        	matrix.postRotate(270);
        	tmpBm = Bitmap.createBitmap(tmpBm, 0, 0, tmpBm.getWidth(), 
        			tmpBm.getHeight(), matrix, true);
        	int matched = Surf.DoSurfCompare(tmpBm, DataWareHouse.Instance().GetTemplate());
        	int templateCount = DataWareHouse.Instance().GetTemplatePointCount();
        	float hitRate = (float)matched / (float)templateCount;
        	float criteria = Float.parseFloat(DataWareHouse.Instance().GetAttribute("MatchCriteria"));
        	if (hitRate >= criteria){
        		printMessage("Picture taken!");
        		printResult("match : " + String.valueOf(hitRate>1f?1f:hitRate));
        		savePicture(data);
        	} else {
        		printMessage("Not match!");
        		printResult("rate : " + hitRate);
        	}
        	onDonePicture();
    	}
    };
    
    /**
     * function pointer for saving picture
     */
    @SuppressWarnings("unused")
	private PictureCallback mSavePicture = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            savePicture(data);
            onDonePicture();
        }
    };
    
    /**
     * State machine onTakePicture event
     */
    private boolean onTakePicture(){
    	boolean result = false;
    	switch (mMatchingState) {
    	case COUNT_DOWN_STATE:
    		mMatchingState = COUNT_DOWN_ANALYSING_STATE;
    		result = true;
    		break;
    	case COUNT_DOWN_ANALYSING_STATE:
    		break;
    	case ANALYSING_STATE:
    		break;
    	case READY_TO_SHOT_STATE:
    		mMatchingState = ANALYSING_STATE;
    		result = true;
    		break;
    	}
    	return result;
    }
    
    /**
     * State machine onDonePicture event
     */
    private void onDonePicture(){
    	switch (mMatchingState) {
    	case COUNT_DOWN_STATE:
    		break;
    	case COUNT_DOWN_ANALYSING_STATE:
    		mMatchingState = COUNT_DOWN_STATE;
    	case ANALYSING_STATE:
    		mMatchingState = READY_TO_SHOT_STATE;
    		break;
    	case READY_TO_SHOT_STATE:
    		break;
    	}
    }   
    
    /** 
     * Check if this device has a camera 
     */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }
    
    /** 
     * A safe way to get an instance of the Camera object. 
     */
    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(1); // attempt to get a Camera in front
        }
        catch (Exception e){
            Log.e("getCameraInstance", e.getMessage());
        }
        return c; // returns null if camera is unavailable
    }
    
    /**
     * release the camera for other usage
     */
    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
    
    /**
     * get all possible resolutions and use the smallest one
     */
    private void initiateCameraMinimumResolution(){
    	if(mCamera!=null){
    		List<Size> resolutions = mCamera.getParameters().getSupportedPictureSizes();
    		int minWidth = 1000;
    		int minHeight = 1000;
    		for(Size size : resolutions){
    			if(size.width<minWidth)
    				minWidth = size.width;
    			if(size.height<minHeight)
    				minHeight = size.height;
    		}
    		mCamera.getParameters().setPictureSize(minWidth, minHeight);
    		mCamera.getParameters().setRotation(270);
    	}
    }
    
    /**
     * common way to show message to user
     */
	private void printMessage(String msg){
		countdown_text.setText(msg);
		countdown_text.invalidate();
	}
	
	/**
	 * common way to show result to user
	 */
	private void printResult(String msg){
		result_text.setText(msg);
		result_text.invalidate();
	}
    
	/**
	 * savePicture to application directory
	 * @param data from camera event 
	 */
	private void savePicture(byte[] data) {
		File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null){
            Log.d(VIEW_LOG_TAG, "Error creating media file, check storage permissions: ");
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
                Log.d("mSavePicture","save file completed: " + pictureFile.getAbsolutePath());
            } catch (FileNotFoundException e) {
                Log.e(VIEW_LOG_TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.e(VIEW_LOG_TAG, "Error accessing file: " + e.getMessage());
            }
        }
	}
	
    /** 
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(DataWareHouse.Instance().
        		GetAttribute("AppDirectory"));
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("getOutputMediaFile", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
            "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }
        return mediaFile;
    }    
    
}