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
 * Created by zhangziran on 2017/12/29.
 */

public class MagneticFragment extends BaseFragment {
    private TextView tvTitile, tvDataX, tvDataY, tvDataZ, tvQuality, tvNotice;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener eventListener;
    private boolean isSuccess = false;

    @Override
    protected int getCurrentView() {
        return R.layout.magnetic_test;
    }

    @Override
    protected void onFragmentCreat() {
        tvTitile = (TextView) view.findViewById(R.id.tv_magnetic_title);
        tvDataX = (TextView) view.findViewById(R.id.tv_magnetic_datax);
        tvDataY = (TextView) view.findViewById(R.id.tv_magnetic_datay);
        tvDataZ = (TextView) view.findViewById(R.id.tv_magnetic_dataz);
        tvQuality = (TextView) view.findViewById(R.id.tv_magnetic_quality);
        tvNotice = (TextView) view.findViewById(R.id.tv_magnetic_notice);
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        if (eventListener == null) {
            eventListener = new SensorListener();
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensor == null) {
            tvNotice.setText(getResources().getString(R.string.magnetic_error));
            if (Config.AUTO_TEST.equals(getMode())) {
                updateDataBase(Config.FAIL);
                handler.obtainMessage(2).sendToTarget();
            }
        } else {
            sensorManager.registerListener(eventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            tvTitile.setText(getResources().getString(R.string.magnetic_data));
            tvNotice.setText(getResources().getString(R.string.magnetic_notice));
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.postDelayed(runnable, 2000);
            }
        }
    }

    private class SensorListener implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            float X = event.values[0];
            float Y = event.values[1];
            float Z = event.values[2];
            tvDataX.setText("X: " + X);
            tvDataY.setText("Y: " + Y);
            tvDataZ.setText("Z: " + Z);
            tvQuality.setText("Quality: " + event.accuracy);
            isSuccess = true;
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
        if (sensorManager != null) {
            sensorManager.unregisterListener(eventListener);
        }
    }

    @Override
    public void onDestroyView() {
        if (tvNotice.getText().toString().equals(getResources().getString(R.string.magnetic_error))) {
            SystemClock.sleep(2000);
        }
        isSuccess = false;
        super.onDestroyView();
    }
}
