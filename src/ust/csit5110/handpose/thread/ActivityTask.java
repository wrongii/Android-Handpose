package ust.csit5110.handpose.thread;

import java.util.TimerTask;
import android.app.Activity;

/**
* This is a timertask because it extends the class java.util.TimerTask. This class
* will be given to the timer (java.util.Timer) as the code to be executed.
*
*/
public abstract class ActivityTask extends TimerTask {
	
	private Activity mParentAct;
	
	public void SetParentActivity(Activity act){
		mParentAct = act;
	}
	
	/**
	* Constructs the object, sets the string to be output in function run()
	* @param str
	*/
	public ActivityTask(String objectName) {
		mParentAct = null;
	}
	
	/**
	* When the timer executes, this code is run.
	*/
	public void run() {
		mParentAct.runOnUiThread(new Runnable(){
            public void run() {
            	timeoutFunction();
             }
		});
	}
	
	/**
	 * Abstract function for derived class to implement
	 */
	abstract protected void timeoutFunction();
}