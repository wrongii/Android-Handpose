package ust.csit5110.handpose.thread;

import ust.csit5110.handpose.util.DataWareHouse;
import android.content.Context;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class DelayedSurface extends SurfaceView implements SurfaceHolder.Callback {
	
	/**
	 * For inversion of dependency
	 */
	public interface CameraContext{
		public abstract void TakePicture();
		public abstract void BindCameraPreview();
		public abstract void LastCounterAction();
	}
	
	private SurfaceHolder mHolder;
	private CameraContext _parentAct;
	
	@SuppressWarnings("deprecation")
	public DelayedSurface(Context context) {
		super(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}
	
	public void SetCameraContext(CameraContext act){
		_parentAct = act;
	}

	public SurfaceHolder GetSurfaceHolder() {
		return mHolder;
	}

	public void surfaceCreated(SurfaceHolder holder) {
		_parentAct.BindCameraPreview();
		long millis=1000;
		int totalTime = Integer.parseInt(DataWareHouse.Instance().GetAttribute("CountTime"));
		int tick = Integer.parseInt(DataWareHouse.Instance().GetAttribute("CountTick"));
		for(int i=1; i<totalTime/tick; i++){
			Handler aHandler=new Handler();
			aHandler.postDelayed(new Runnable(){
				public void run() {
					_parentAct.TakePicture();
				} 
			}, tick*millis*i);
		}
		// last tick to change the state of CameraContext
		Handler lHandler=new Handler();
		lHandler.postDelayed(new Runnable(){
			public void run() {
				_parentAct.LastCounterAction();
			}
		}, totalTime*millis);
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// nothing to clean up
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// nothing to change
	}
}
