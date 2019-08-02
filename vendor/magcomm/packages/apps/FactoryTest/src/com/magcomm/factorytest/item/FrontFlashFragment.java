package com.magcomm.factorytest.item;

import android.hardware.Camera;
import android.util.Log;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/29.
 */

public class FrontFlashFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private Camera camera;

    @Override
    protected int getCurrentView() {
        return R.layout.flash_test;
    }

    @Override
    protected void onFragmentCreat() {
        Log.i(TAG, "onFragmentCreat: FrontFlashFragment");
        try {
            camera = Camera.open(1);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(parameters);
            handler.postDelayed(runnable, 2000);
        } catch (Exception e) {
            destroyByMode(Config.FAIL, 0);
        }
    }


    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            destroyByMode(Config.SUCCESS, 1);
        }
    };

    private void destroyByMode(String sResult, int iResult) {
        if (Config.AUTO_TEST.equals(getMode())) {
            handler.obtainMessage(2).sendToTarget();
            updateDataBase(sResult);
        } else if (Config.MANUAL_TEST.equals(getMode())) {
            if (iResult == 0) {
                handler.obtainMessage(iResult).sendToTarget();
            }
        }
    }

    @Override
    public void destroy() {
        handler.removeCallbacks(runnable);
        runnable = null;
        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            if (parameters.getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
            camera.setParameters(parameters);
            camera.release();
            camera = null;
        }
    }
}
