package com.magcomm.factorytest.item;

import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.activity.ItemTestActivity;
import com.magcomm.factorytest.util.Config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by zhangziran on 2017/12/29.
 */

public class ViceCameraFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private FrameLayout cameraContain;
    private int surfaceWidth, surfaceHeight;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SurfaceHolder.Callback holderCallback;
    private Camera camera;
    private TreeMap<Integer, Camera.Size> treeMap;
    private volatile static boolean isNeedRunning = true;
    private FileThread fileThread;

    @Override
    protected int getCurrentView() {
        return R.layout.vice_camera_test;
    }

    @Override
    protected void onFragmentCreat() {
        cameraContain = (FrameLayout) view.findViewById(R.id.fl_vice_camera_contain);
        initView();
        initCamera();
    }

    private void initView() {
        int windowWidth = getResources().getDisplayMetrics().widthPixels;
        int windowHeight = getResources().getDisplayMetrics().heightPixels;
        surfaceWidth = windowWidth;
        surfaceHeight = (int) ((float) windowWidth / 0.75);
        surfaceView = new SurfaceView(context);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(surfaceWidth, surfaceHeight);
        layoutParams.gravity = Gravity.BOTTOM;
        cameraContain.addView(surfaceView, layoutParams);

        TextView textView = new TextView(context);
        textView.setText(context.getResources().getString(R.string.vice_camera_notice));
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(textView.getTextSize()*0.7f);
        FrameLayout.LayoutParams textLayoutParams = new FrameLayout.LayoutParams(surfaceWidth, windowHeight-surfaceHeight);
        textLayoutParams.gravity = Gravity.TOP;
        cameraContain.addView(textView,textLayoutParams);

    }

    private void initCamera() {
        if (holderCallback == null) {
            holderCallback = new SurfaceHolderCallback();
        }
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(holderCallback);
        surfaceHolder.setKeepScreenOn(true);
    }

    private class SurfaceHolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(holder);
                if (fileThread != null) {
                    isNeedRunning = false;
                    fileThread.interrupt();
                    fileThread = null;
                }
                isNeedRunning = true;
                fileThread = new FileThread();
                fileThread.start();
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

    private void destroyByMode(String sResult, int iResult) {
        if (iResult == 0) {
            handler.obtainMessage(iResult).sendToTarget();
        }
    }

    private class FileThread extends Thread {
        private Runnable fileRunnable = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, context.getResources().getString(R.string.vice_camera_toast), Toast.LENGTH_SHORT).show();
            }
        };

        @Override
        public void run() {
            while (isNeedRunning) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (isCoverDualCam()) {
                    ((ItemTestActivity) context).runOnUiThread(fileRunnable);
                }
            }
        }

        private boolean isCoverDualCam() {
            File file = new File("/proc/driver", "dualcam_flag");
            if (!file.exists()) return false;
            FileInputStream out = null;
            InputStreamReader isr = null;
            try {
                out = new FileInputStream(file);
                isr = new InputStreamReader(out);
                int ch = 0;
                while ((ch = isr.read()) != -1) {
                    String mode = "" + (char) ch;
                    boolean isCoverDualCam = "1".equals(mode);
                    return isCoverDualCam;
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out != null) out.close();
                    if (isr != null) isr.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return false;
        }
    }


    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        if(fileThread!=null) {
            isNeedRunning = false;
            fileThread.interrupt();
            fileThread = null;
        }
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
