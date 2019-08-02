package com.magcomm.factorytest.item;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

/**
 * Created by zhangziran on 2017/12/29.
 */

public class BackCameraFragment extends BaseFragment implements View.OnClickListener {
    private static final String TAG = "zhangziran";
    private FrameLayout cameraContain;
    private int surfaceWidth, surfaceHeight;
    private SurfaceView surfaceView;
    private static final int BTN_ID = 0x1000;
    private SurfaceHolder surfaceHolder;
    private SurfaceHolder.Callback holderCallback;
    private Camera camera;
    private Button button;
    private TreeMap<Integer, Camera.Size> treeMap;
    private CameraRunnable cameraRunnable;
    private CameraObservable cameraObservable;

    @Override
    protected int getCurrentView() {
        return R.layout.camera_test;
    }

    @Override
    protected void onFragmentCreat() {
        cameraContain = (FrameLayout) view.findViewById(R.id.fl_camera_contain);
        initView();
        initCamera();
    }

    private void initView() {
        int windowWidth = getResources().getDisplayMetrics().widthPixels;
        surfaceWidth = windowWidth;
        surfaceHeight = (int) ((float) windowWidth / 0.75);
        surfaceView = new SurfaceView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(surfaceWidth, surfaceHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        cameraContain.addView(surfaceView, layoutParams);
        button = new Button(context);
        button.setText(getResources().getString(R.string.take_picture));
        button.setId(BTN_ID);
        button.setOnClickListener(this);
        FrameLayout.LayoutParams layoutParam = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        layoutParam.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        cameraContain.addView(button, layoutParam);
    }

    private void initCamera() {
        if (holderCallback == null) {
            holderCallback = new SurfaceHolderCallback();
        }
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(holderCallback);
        surfaceHolder.setKeepScreenOn(true);
        if (Config.AUTO_TEST.equals(getMode())) {
            button.setEnabled(false);
        }
    }

    private class SurfaceHolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(holder);
                if (Config.AUTO_TEST.equals(getMode())) {
                    cameraRunnable = new CameraRunnable();
                    cameraObservable = new CameraObservable();
                    cameraObservable.addObserver(cameraRunnable);
                    handler.postDelayed(cameraRunnable, 4000);
                }
            } catch (Exception e) {
                destroyByMode(Config.FAIL, 0);
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size size = getSize(sizes);
            parameters.setPreviewSize(size.width, size.height);
            parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            camera.setParameters(parameters);
            camera.startPreview();
            camera.cancelAutoFocus();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview();
        }
    }

    private Camera.Size getSize(List<Camera.Size> sizes) {
        if (treeMap != null) {
            treeMap.clear();
            treeMap = null;
        }
        treeMap = new TreeMap<>();
        for (Camera.Size size : sizes) {
            int width = size.width;
            int height = size.height;
            float hw = (float) height / width;
            if (hw == 0.75) {
                treeMap.put(width, size);
            }
        }
        int key = treeMap.lastKey() > treeMap.firstKey() ? treeMap.lastKey() : treeMap.firstKey();
        return treeMap.get(key);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == BTN_ID) {
            camera.takePicture(null, null, picCallback);
            v.setEnabled(false);
        }
    }

    private Camera.PictureCallback picCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera1) {
            camera.startPreview();
            try {
                String path1 = "/sdcard/pic";
                File file1 = new File(path1);
                if (!file1.exists()) {
                    file1.mkdir();
                }
                String path = path1 + File.separator + System.currentTimeMillis() + ".jpg";
                File file = new File(path);
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(file);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                button.setEnabled(true);
                if (Config.AUTO_TEST.equals(getMode())) {
                    cameraObservable.notifyCamera();
                }
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void deleteFile() {
        String path = "/sdcard/pic";
        File file = new File(path);
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f != null) {
                    f.delete();
                }
            }
            file.delete();
        }
    }

    private class CameraRunnable implements Runnable, Observer {
        @Override
        public void run() {
            if (button != null) {
                button.performClick();
            }
        }

        @Override
        public void update(Observable observable, Object o) {
            if (checkFile()) {
                destroyByMode(Config.SUCCESS, -999);
            } else {
                destroyByMode(Config.FAIL, -999);
            }
        }
    }

    private class CameraObservable extends Observable {
        private void notifyCamera() {
            setChanged();
            notifyObservers();
        }
    }


    private boolean checkFile() {
        boolean result = false;
        String path = "/sdcard/pic";
        File file = new File(path);
        File[] files = file.listFiles();
        Log.i(TAG, "checkFile: file.exists()=" + file.exists() + "--files.length=" + files.length);
        if (file.exists() && files.length > 0) {
            result = true;
        }
        return result;
    }

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
        if (Config.AUTO_TEST.equals(getMode())) {
            cameraObservable.deleteObservers();
            button.setEnabled(true);
        }
        deleteFile();
        if (surfaceHolder != null) {
            surfaceHolder.removeCallback(holderCallback);
        }
        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
        }
    }
}
