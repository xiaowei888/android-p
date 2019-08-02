package com.magcomm.factorytest.item;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import android.widget.ImageView;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/29.
 */

public class AccelerometerFragment extends BaseFragment {
    private TextView tvTitle, tvDataX, tvDataY, tvDataZ, tvNotice;
    private ImageView ivDirection;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener eventListener;
    private Drawable up, down, left, right;
    private boolean isSuccess = false;

    @Override
    protected int getCurrentView() {
        return R.layout.accelerometer_test;
    }

    @Override
    protected void onFragmentCreat() {
        tvTitle = (TextView) view.findViewById(R.id.tv_accelerometer_title);
        tvDataX = (TextView) view.findViewById(R.id.tv_accelerometer_datax);
        tvDataY = (TextView) view.findViewById(R.id.tv_accelerometer_datay);
        tvDataZ = (TextView) view.findViewById(R.id.tv_accelerometer_dataz);
        tvNotice = (TextView) view.findViewById(R.id.tv_accelerometer_notice);
        ivDirection = (ImageView) view.findViewById(R.id.iv_direction);
        up = context.getDrawable(R.mipmap.up);
        down = context.getDrawable(R.mipmap.down);
        left = context.getDrawable(R.mipmap.left);
        right = context.getDrawable(R.mipmap.right);
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        if (eventListener == null) {
            eventListener = new SensorListener();
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor == null) {
            tvNotice.setText(getResources().getString(R.string.accelerometer_notice));
            if (Config.AUTO_TEST.equals(getMode())) {
                updateDataBase(Config.FAIL);
                handler.obtainMessage(2).sendToTarget();
            }
        } else {
            sensorManager.registerListener(eventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            tvTitle.setText(getResources().getString(R.string.accelerometer_data));
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.postDelayed(runnable, 2000);
            }
        }
    }

    private class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float X = event.values[SensorManager.DATA_X];
            float Y = event.values[SensorManager.DATA_Y];
            float Z = event.values[SensorManager.DATA_Z];
            tvDataX.setText("X:" + X);
            tvDataY.setText("Y:" + Y);
            tvDataZ.setText("Z:" + Z);
            ivDirection.setImageDrawable(getDrawable(X, Y));
            isSuccess = true;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private Drawable getDrawable(float x, float y) {
        Drawable drawable = null;
        float X = Math.abs(x);
        float Y = Math.abs(y);
        if (X > Y) {
            if (x > 0) {
                drawable = left;
            } else {
                drawable = right;
            }
        } else {
            if (y > 0) {
                drawable = down;
            } else {
                drawable = up;
            }
        }
        return drawable;
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
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        runnable = null;
        if (sensorManager != null) {
            sensorManager.unregisterListener(eventListener);
        }
    }

    @Override
    public void onDestroyView() {
        if (tvNotice.getText().toString().equals(getResources().getString(R.string.accelerometer_notice)) && Config.AUTO_TEST.equals(getMode())) {
            SystemClock.sleep(2000);
        }
        super.onDestroyView();
    }
}
