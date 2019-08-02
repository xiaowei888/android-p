package com.magcomm.factorytest.sensor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EmSensorReceiver extends BroadcastReceiver{
    private static final String TAG="EmSensorReceiver";
    private static final int TOLERANCE_20=2;
    private static final String mAction="com.mediatek.emgineermode.action_sensor_emsensor";
	private static final String CLEARACTION="com.mediatek.emgineermode.action_sensor_clearsensor";
    private static final String mReturnAction="com.magcomm.cit.action_result";
	private static final String mClearAction="com.magcomm.cit.action_clear";
    @Override
    public void onReceive(Context context, Intent intent) {
		String action=null;
		int result=0;
        if (mAction.equals(intent.getAction())) {
			action=mReturnAction;
			result=EmSensor.doGsensorCalibration(TOLERANCE_20);
		} else if (CLEARACTION.equals(intent.getAction())) {
			action=mClearAction;
			result=EmSensor.clearGsensorCalibration();
		}
		if(action!=null){
			Intent intentResult=new Intent(action);
            intentResult.putExtra("result",result==1? true : false);
			context.sendBroadcast(intentResult);
		}
    }
}
