package com.magcomm.teecheck;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;

public class TeeCheckActivity extends Activity{

	private TextView tv ;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("teejni");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        tv = (TextView) findViewById(R.id.tee_status);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v("TeeCheckActivity", "getDeviceStatus is " + getDeviceStatus()
					+ ", getTeeKeyStatus is " + getTeeKeyStatus()
					+ ", getKeyboxStatus is " + getKeyboxStatus());
        if((getDeviceStatus() == 0) && (getTeeKeyStatus() == 0) && (getKeyboxStatus() == 0)) {
			tv.setText("SUCCESS");	
		} else {
			tv.setText("FAILED");
		}
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native int getTeeKeyStatus();
    public native int getDeviceStatus();
    public native int getKeyboxStatus();
}
