package com.magcomm.factorytest.batterytest.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.batterytest.activity.BatteryBaseActivity;

import java.util.Arrays;
import java.util.TreeMap;

/**
 * Created by zhangziran on 2017/12/1.
 */

public class Camera2TestFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private View view;
    private TextureView textureView;
    private RelativeLayout textureContain;
    private static TextureView.SurfaceTextureListener textureListener;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private String cameraID = FRONT_CAMERA;
    private static final String FRONT_CAMERA = CameraCharacteristics.LENS_FACING_FRONT + "";
    private static final String BACK_CAMERA = CameraCharacteristics.LENS_FACING_BACK + "";
    private int surfaceWidth;
    private int surfaceHeight;
    private Handler handler;
    private boolean isFirst = true;
    private TreeMap<Integer, Size> treeMap;

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
        view = inflater.inflate(R.layout.camera_two_test, null);
        //textureView = view.findViewById(R.id.texture_view);
        initView();
        return view;
    }

    private void initView() {
        textureContain = (RelativeLayout) view.findViewById(R.id.camera_texture_contain);
        int windowWidth = getActivity().getResources().getDisplayMetrics().widthPixels;
        int textureWidth = windowWidth;
        int textureHeight = (int) ((float) windowWidth / 0.75);
        Log.i(TAG, "initView: windowWidth=" + windowWidth + "--textureHeight=" + textureHeight);
        textureView = new TextureView(getActivity());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textureWidth, textureHeight);
        textureContain.addView(textureView, layoutParams);
        textureListener = new CameraTextureListener();
        textureView.setSurfaceTextureListener(textureListener);
        cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
    }

    private class CameraTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            surfaceWidth = width;
            surfaceHeight = height;
            initCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initCamera() {
        cameraID = getModel() == 0 ? FRONT_CAMERA : BACK_CAMERA;
        try {
            cameraManager.openCamera(cameraID, stateCallback, handler);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            reset(true);
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            reset(true);
        }
    };
    private CaptureRequest.Builder build;

    private void createCameraPreview() {
        try {
            SurfaceTexture surfaceTexture = textureView.getSurfaceTexture();
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            for (Size se : sizes) {
                Log.i(TAG, "createCameraPreview: width=" + se.getWidth() + "--height=" + se.getHeight());
            }
            Size size = getSize(sizes);
            surfaceTexture.setDefaultBufferSize(size.getWidth(), size.getHeight());
            Log.i(TAG, "createCameraPreview: size.getWidth()=" + size.getWidth() + "--size.getHeight()=" + size.getHeight());
            Surface surface = new Surface(surfaceTexture);
            build = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            build.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            build.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            build.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    try {
                        //自动对焦
                        build.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                        build.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
                        session.setRepeatingRequest(build.build(), captureCallback, handler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                }
            }, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size getSize(Size[] sizes) {
        if (treeMap != null) {
            treeMap.clear();
            treeMap = null;
        }
        treeMap = new TreeMap<>();
        for (Size size : sizes) {
            int width = size.getWidth();
            int height = size.getHeight();
            float hw = (float) height / width;
            if (hw == 0.75) {
                treeMap.put(width, size);
            }
        }
        int key = treeMap.lastKey() > treeMap.firstKey() ? treeMap.lastKey() : treeMap.firstKey();
        return treeMap.get(key);
    }

    private CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
            if (isFirst) {
                handler.postDelayed(runnable, 5000);
                isFirst = false;
            }
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
            reset(true);
            super.onCaptureFailed(session, request, failure);
        }
    };
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            reset(false);
        }
    };

    public void reset(boolean fail) {
        if (fail) {
            Toast.makeText(getActivity(), "Camera run error", Toast.LENGTH_LONG).show();
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        isFirst = true;
        handler.obtainMessage(0).sendToTarget();
    }

    @Override
    public void onDestroyView() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        isFirst = true;
        handler.removeCallbacks(runnable);
        super.onDestroyView();
    }
}
