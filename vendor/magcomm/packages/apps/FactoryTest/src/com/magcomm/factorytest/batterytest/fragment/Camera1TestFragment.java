package com.magcomm.factorytest.batterytest.fragment;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.batterytest.activity.BatteryBaseActivity;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by zhangziran on 2017/12/15.
 */

public class Camera1TestFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private View view;
    private Handler handler;
    private RelativeLayout surfaceContain;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceHolderCallback holderCallback;
    private Camera camera;
    private int surfaceWidth;
    private int surfaceHeight;
    private TreeMap<Integer,Camera.Size> treeMap;
    private Camera.PreviewCallback previewCallback;
    private boolean isFirst = true;
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
        view = inflater.inflate(R.layout.camera_one_test, null);
        initView();
        initCamera();
        return view;
    }

    private void initView() {
        surfaceContain = (RelativeLayout) view.findViewById(R.id.camera_surface_contain);
        int windowWidth = getActivity().getResources().getDisplayMetrics().widthPixels;
        surfaceWidth = windowWidth;
        surfaceHeight = (int) ((float) windowWidth / 0.75);
        surfaceView = new SurfaceView(getActivity());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(surfaceWidth, surfaceHeight);
        surfaceContain.addView(surfaceView, layoutParams);
    }

    private void initCamera() {
        surfaceHolder = surfaceView.getHolder();
        if (holderCallback != null) {
            holderCallback = null;
        }
        if(previewCallback!=null) {
            previewCallback = null;
        }
        previewCallback =  new CameraPreviewCallback();
        holderCallback = new SurfaceHolderCallback();
        surfaceHolder.addCallback(holderCallback);
    }

    private class SurfaceHolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open(getModel());
                camera.setPreviewDisplay(holder);
                camera.setDisplayOrientation(90);
            } catch (Exception e) {
                handler.obtainMessage(0).sendToTarget();
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size size = getSize(sizes);
            parameters.setPreviewSize(size.width, size.height);
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            //parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            //parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            camera.setParameters(parameters);
            camera.startPreview();
            camera.setPreviewCallback(previewCallback);
            camera.cancelAutoFocus();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
        }
    }
    private Camera.Size getSize(List<Camera.Size> sizes) {
        if(treeMap!=null) {
            treeMap.clear();
            treeMap = null;
        }
        treeMap = new TreeMap<>();
        for (Camera.Size size : sizes) {
            int width = size.width;
            int height = size.height;
            float hw = (float) height / width;
            if(hw == 0.75) {
                treeMap.put(width,size);
            }
        }
        int key = treeMap.lastKey()>treeMap.firstKey()?treeMap.lastKey():treeMap.firstKey();
        return treeMap.get(key);
    }

    private class CameraPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if(isFirst) {
                isFirst = false;
                handler.postDelayed(runnable,5000);
            }
        }
    }
    private Runnable runnable =new Runnable() {
        @Override
        public void run() {
            handler.obtainMessage(0).sendToTarget();
        }
    };

    @Override
    public void reset(boolean fail) {
        isFirst = true;
        handler.removeCallbacks(runnable);
        if(surfaceHolder!=null) {
            surfaceHolder.removeCallback(holderCallback);
        }
        if(camera!=null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }


    @Override
    public void onDestroyView() {
        reset(false);
        super.onDestroyView();
    }
}
