package fsu.android.RotaryDialer;

import fsu.android.RotaryDialer.DialerView.DialerThread;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

public class RotaryDialer extends Activity {
	DialerView mDialerView;
	DialerThread mDialerThread;
	
@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	
	//get objects
	SurfaceView dialSpace = (SurfaceView) findViewById(R.id.DrawSpace);
	SurfaceHolder holder = dialSpace.getHolder();
	Button Call = (Button) findViewById(R.id.bCall);
	Button Mute = (Button) findViewById(R.id.bMute);
	EditText Number = (EditText) findViewById(R.id.tNumber);
	RelativeLayout Parent = (RelativeLayout) findViewById(R.id.RelativeLayout);
	

	// turn off the window's title bar
    //requestWindowFeature(Window.FEATURE_NO_TITLE);

	mDialerView = (DialerView) findViewById(R.id.DrawSpace);
	mDialerThread = mDialerView.getThread();
	//mDialerView.setMinimumHeight(Parent.getHeight() - mDialerView.getTop() - 
		//						 Call.getHeight() - 40);
	
	mDialerView.setMinimumHeight(120);
    // give the View a handle to the TextView used for messages
    mDialerView.setTextView(Number);

    if (savedInstanceState == null) {
        // we were just launched: set up a new game
        mDialerThread.setState(DialerThread.STATE_READY);
        Log.w(this.getClass().getName(), "SIS is null");
    } else {
        // we are being restored: resume a previous game
    	mDialerThread.restoreState(savedInstanceState);
        Log.w(this.getClass().getName(), "SIS is nonnull");
    }

    mDialerThread.doStart();
}

@Override
protected void onPause() {
	super.onPause();
	mDialerView.getThread().pause();
}

@Override
protected void onSaveInstanceState(Bundle outState) {
	super.onSaveInstanceState(outState);
	mDialerThread.saveState(outState);
	Log.w(this.getClass().getName(), "SIS called");
}

} //end of activity