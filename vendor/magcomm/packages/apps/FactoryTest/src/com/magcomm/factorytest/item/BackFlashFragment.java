package com.magcomm.factorytest.item;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/29.
 */

public class BackFlashFragment extends BaseFragment {
    private CameraManager cameraManager;

    @Override
    protected int getCurrentView() {
        return R.layout.flash_test;
    }

    @Override
    protected void onFragmentCreat() {
        cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.setTorchMode("0", true);
            handler.postDelayed(runnable, 2000);
        } catch (CameraAccessException e) {
            destroyByMode(Config.FAIL, 0);
            e.printStackTrace();
        }
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
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        try {
            if (cameraManager != null) {
                cameraManager.setTorchMode("0", false);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
