package com.magcomm.settingtools.sensorsettings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.res.Configuration;
import android.widget.FrameLayout;
import com.magcomm.factorytest.sensor.EmSensor;
import com.magcomm.factorytest.R;
import android.os.Handler;


public class GravityActivity extends Activity {

    private TextView xPoint,yPoint,zPoint;
    private float x,y,z;
    private GravityAnimView mAnimView;
    private Button btn_clr_calibration;
    private Button bt_calibration;
	private SensorManager sm;
	private SensorEventListener sel;
	private Sensor sensor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gravity_layout);
        xPoint=(TextView) findViewById(R.id.x_point);
        yPoint=(TextView) findViewById(R.id.y_point);
        zPoint=(TextView) findViewById(R.id.z_point);
        mAnimView= (GravityAnimView) findViewById(R.id.gravity_View);
		mAnimView.setVisibility(isInMultiWindowMode()? View.GONE : View.VISIBLE);
        btn_clr_calibration= (Button) findViewById(R.id.reset);
        bt_calibration= (Button) findViewById(R.id.calibration);
        btn_clr_calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        bt_calibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
				setButtonEnabled(false);
				mHandler.post(new Runnable() {
           			@Override
            		public void run() {
               			int result=EmSensor.doGsensorCalibration(2);
						requestResult(result);
						setButtonEnabled(true);
            		}
        		});
            }
        });
        sm=(SensorManager) this.getSystemService(SENSOR_SERVICE);
        sensor=sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sel=new SensorEventListener(){
            public void onSensorChanged(SensorEvent se) {
                x=se.values[SensorManager.DATA_X];
                y=se.values[SensorManager.DATA_Y];
                z=se.values[SensorManager.DATA_Z];
                xPoint.setText("X: "+managerValue(x));
                yPoint.setText("Y: "+managerValue(y));
                zPoint.setText("Z: "+managerValue(z));
                mAnimView.setValue(x,y);
            }
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }
        };
    }
	@Override
	protected void onPause(){
		super.onPause();
		sm.unregisterListener(sel);
		//mAnimView.stop();
	}
	@Override
	protected void onResume(){
		super.onResume();
        sm.registerListener(sel, sensor,SensorManager.SENSOR_DELAY_UI);
		//mAnimView.start();
	}
    private String managerValue(float value){
        return String.format("%.3f", value);
    }
    private void requestResult(int result){
        Toast.makeText(this, (result==1)? getString(R.string.operate_ok) : getString(R.string.operate_fail),Toast.LENGTH_SHORT).show();
    }
	@Override
	public void onConfigurationChanged(Configuration newConfig){
		super.onConfigurationChanged(newConfig);
		
	}
	Handler mHandler =new Handler();
	private void sendInfo(){
		
	}
	private void setButtonEnabled(boolean enable){
        btn_clr_calibration.setEnabled(enable);
        bt_calibration.setEnabled(enable);
    }
	@Override
	public void onMultiWindowModeChanged(boolean inMultiWindow){
		super.onMultiWindowModeChanged(inMultiWindow);
		mAnimView.setVisibility(inMultiWindow? View.GONE : View.VISIBLE);
	}
    private void showDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(R.string.res_calibration);
        builder.setPositiveButton(R.string.ok,
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
						setButtonEnabled(false);
						mHandler.post(new Runnable() {
           				@Override
            				public void run() {
                        	int result=EmSensor.clearGsensorCalibration();
							requestResult(result);
							setButtonEnabled(true);
            			}
        			});
                    }
                });
        builder.setNeutralButton(R.string.cancel,
                new android.content.DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }
}
