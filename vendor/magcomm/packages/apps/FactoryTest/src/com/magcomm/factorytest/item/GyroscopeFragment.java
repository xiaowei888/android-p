package com.magcomm.factorytest.item;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/27.
 */

public class GyroscopeFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener sensorEventListener;
    private TextView tvTitle, tvDataX, tvDataY, tvDataZ, tvNotice;
    private static boolean isFirst = true;

    @Override
    protected int getCurrentView() {
        return R.layout.gyroscope_test;
    }

    @Override
    protected void onFragmentCreat() {
        initView();
        initGyroscope();
    }

    private void initView() {
        tvTitle = (TextView) view.findViewById(R.id.tv_gyroscope_title);
        tvDataX = (TextView) view.findViewById(R.id.tv_gyroscope_datax);
        tvDataY = (TextView) view.findViewById(R.id.tv_gyroscope_datay);
        tvDataZ = (TextView) view.findViewById(R.id.tv_gyroscope_dataz);
        tvNotice = (TextView) view.findViewById(R.id.tv_gyroscope_notice);
    }

    private void initGyroscope() {
        if (sensorManager == null) {
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        if (sensorEventListener == null) {
            sensorEventListener = new GyroscopeListener();
        }
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        if (sensor == null) {
            tvNotice.setText(getResources().getString(R.string.gyroscope_notice));
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.obtainMessage(2).sendToTarget();
                updateDataBase(Config.FAIL);
            }
        } else {
            sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
            tvTitle.setText(getResources().getString(R.string.gyroscope_data));
            tvDataX.setText("X:-1");
            tvDataY.setText("Y:-1");
            tvDataZ.setText("Z:-1");
        }
    }

    private class GyroscopeListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            tvDataX.setText("X:" + x);
            tvDataY.setText("Y:" + y);
            tvDataZ.setText("Z:" + z);
            if (isFirst) {
                isFirst = false;
                if (Config.AUTO_TEST.equals(getMode())) {
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.SUCCESS);
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        isFirst = true;
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }
}
