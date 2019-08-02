package com.magcomm.factorytest.item;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;
import com.magcomm.factorytest.util.FactoryTestJNI;

/**
 * Created by zhangziran on 2017/12/29.
 */

public class LightSensorFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private TextView tvLightSensor;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener eventListener;
    private boolean flag;
    private FactoryTestJNI factoryTestJNI;
    private int lightValues;
    private static boolean isNeedRunning = true;

    @Override
    protected int getCurrentView() {
        return R.layout.lightsensor_test;
    }

    @Override
    protected void onFragmentCreat() {
        tvLightSensor = (TextView) view.findViewById(R.id.tv_light);
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        if (eventListener == null) {
            eventListener = new SensorListener();
        }
        if (factoryTestJNI == null) {
            factoryTestJNI = new FactoryTestJNI();
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        flag = sensorManager.registerListener(eventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        if (flag) {
            handler.post(runnable);
        } else {
            tvLightSensor.setText(R.string.light_sensor_notice);
            if (Config.AUTO_TEST.equals(getMode())) {
                updateDataBase(Config.FAIL);
                handler.obtainMessage(2).sendToTarget();
            }
        }
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isNeedRunning) {
                lightValues = factoryTestJNI.getALSValue();
                tvLightSensor.setText(context.getResources().getString(R.string.light_intensity_value) + lightValues);
                handler.postDelayed(this, 500);
                if (Config.AUTO_TEST.equals(getMode())) {
                    if (lightValues >= 0) {
                        isNeedRunning = false;
                        handler.removeCallbacks(runnable);
                        updateDataBase(Config.SUCCESS);
                        handler.obtainMessage(2).sendToTarget();
                    }else {
                        isNeedRunning = false;
                        handler.removeCallbacks(runnable);
                        updateDataBase(Config.FAIL);
                        handler.obtainMessage(2).sendToTarget();
                    }
                }
            }
        }
    };

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(eventListener);
        }
        isNeedRunning = false;
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isNeedRunning = true;
        runnable = null;
        if (tvLightSensor.getText().toString().equals(getResources().getString(R.string.light_sensor_notice)) && Config.AUTO_TEST.equals(getMode())) {
            SystemClock.sleep(2000);
        }
    }
}
