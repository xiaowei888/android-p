package com.magcomm.factorytest.batterytest.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.batterytest.activity.BatteryBaseActivity;

/**
 * Created by zhangziran on 2017/12/1.
 */

public class FlashLightTestFragment extends BaseFragment {
    private View view;
    private Handler handler;
    private CameraManager cameraManager;
    private static final String FRONT_CAMERA = CameraCharacteristics.LENS_FACING_FRONT + "";

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof BatteryBaseActivity) {
            handler = ((BatteryBaseActivity) activity).getHandler();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.flash_test, null);
        initLight();
        return view;
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void initLight() {
        try {
            cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
            cameraManager.setTorchMode(FRONT_CAMERA,true);
            handler.postDelayed(runnable,5000);
        } catch (CameraAccessException e) {
            Toast.makeText(getActivity(),"Light open fail",Toast.LENGTH_LONG).show();
            handler.removeCallbacks(runnable);
            handler.obtainMessage(0).sendToTarget();
            e.printStackTrace();
        }
    }

    private Runnable runnable = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void run() {
            handler.obtainMessage(0).sendToTarget();
            try {
                cameraManager.setTorchMode(FRONT_CAMERA,false);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

        }
    };


    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void reset(boolean fail) {
        handler.removeCallbacks(runnable);
        try {
            cameraManager.setTorchMode(FRONT_CAMERA,false);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

}
