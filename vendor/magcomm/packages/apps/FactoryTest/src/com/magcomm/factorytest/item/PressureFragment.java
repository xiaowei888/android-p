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

/**
 * Created by zhangziran on 2018/1/18.
 */

public class PressureFragment extends BaseFragment {
    private TextView tvPressure;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener eventListener;
    private boolean isSuccess = false;

    @Override
    protected int getCurrentView() {
        return R.layout.pressure_test;
    }

    @Override
    protected void onFragmentCreat() {
        tvPressure = (TextView) view.findViewById(R.id.tv_pressure);
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        if (sensor != null) {
            eventListener = new PressureListener();
            sensorManager.registerListener(eventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.postDelayed(runnable, 2000);
            }
        } else {
            tvPressure.setText(getResources().getString(R.string.no_pressure));
            if (Config.AUTO_TEST.equals(getMode())) {
                updateDataBase(Config.FAIL);
                handler.obtainMessage(2).sendToTarget();
            }
        }
    }

    private class PressureListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float pressure = event.values[0];
            tvPressure.setText(pressure + "hPa");
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isSuccess) {
                isSuccess = false;
                handler.removeCallbacks(runnable);
                updateDataBase(Config.SUCCESS);
                handler.obtainMessage(2).sendToTarget();
                return;
            }
            handler.postDelayed(runnable, 2000);
        }
    };

    @Override
    public void destroy() {
        runnable = null;
        if (sensorManager != null) {
            sensorManager.unregisterListener(eventListener);
        }
    }

    @Override
    public void onDestroyView() {
        if (tvPressure.getText().toString().equals(getResources().getString(R.string.no_pressure)) && Config.AUTO_TEST.equals(getMode())) {
            SystemClock.sleep(2000);
        }
        super.onDestroyView();
    }
}
