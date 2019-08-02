package com.magcomm.factorytest.batterytest.fragment;


import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.batterytest.activity.BatteryBaseActivity;

import java.io.IOException;

/**
 * Created by zhangziran on 2017/11/30.
 */

public class VideoTestFragment extends BaseFragment {
    private View view;
    private Handler handler;
    private TextureView textureView;
    private RelativeLayout texureContain;
    private MediaPlayer mediaPlayer;
    private AssetManager assetManager;
    private AudioManager audioManager;
    private TextureView.SurfaceTextureListener textureListener;
    private MediaPlayer.OnPreparedListener preparedListener;
    private MediaPlayer.OnCompletionListener completionListener;
    private static final String TAG = "zhangziran";

    private static final int UNKNOW =0;
    private static final int PREPARE =1;
    private static final int START = 2;
    private static final int PAUSE = 3;
    private static final int STOP = 4;
    private static int status =UNKNOW;
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
        view = inflater.inflate(R.layout.video_test, null);
        initView();
        return view;
    }

    private void initView() {
        texureContain = (RelativeLayout)view.findViewById(R.id.texture_contain);
        int windowWidth = getActivity().getResources().getDisplayMetrics().widthPixels;
        int textureViewWidth = 352;
        int textureViewHeidht = 288;
        if(windowWidth<=textureViewWidth) {
            textureViewWidth = windowWidth;
        }else {
            textureViewHeidht = (int)((float)windowWidth/textureViewWidth)*textureViewHeidht;
            textureViewWidth =  windowWidth;
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(textureViewWidth,textureViewHeidht);
        textureView = new TextureView(getActivity());
        texureContain.addView(textureView,layoutParams);
    }

    @Override
    public void onStart() {
        super.onStart();
        init();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(status == PAUSE) {
            mediaPlayer.start();
        }
    }

    private void init() {
        assetManager = getActivity().getAssets();
        audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        initMediaPlayer();
        initTextureView();
    }

    private void initMediaPlayer() {
        if (mediaPlayer != null) {
            reset(false);
        }
	mediaPlayer = new MediaPlayer();
        switch (model) {
            case 0:
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                }
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
                break;
            case 1:
                if (!audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(true);
                }
                audioManager.setMode(AudioManager.MODE_NORMAL);
                volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                break;
        }
        mediaPlayer.setScreenOnWhilePlaying(true);
        if (preparedListener != null) {
            preparedListener = null;
        }
        if (completionListener != null) {
            completionListener = null;
        }
        completionListener = new VideoCompletionListener();
        preparedListener = new VideoPreparedListener();
        mediaPlayer.setOnPreparedListener(preparedListener);
        mediaPlayer.setOnCompletionListener(completionListener);
    }

    private class VideoPreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) {
            mediaPlayer.start();
            status = START;
        }
    }

    private class VideoCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            handler.obtainMessage(0).sendToTarget();
        }
    }

    private void initTextureView() {
        if (textureListener != null) {
            textureListener = null;
        }
        textureListener = new VideoTextureView();
        textureView.setSurfaceTextureListener(textureListener);
    }

    @TargetApi(24)
    private class VideoTextureView implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            try {
                AssetFileDescriptor fileDescriptor = assetManager.openFd("testvideo.3gp");
                //320*240
                //352*288
                mediaPlayer.setDataSource(fileDescriptor);
                mediaPlayer.setSurface(new Surface(surfaceTexture));
                mediaPlayer.prepareAsync();
                status = PREPARE;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
            reset(false);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
        }
    }

    public void reset(boolean fail) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
            status =UNKNOW;
        }
        if (preparedListener != null) {
            preparedListener = null;
        }
        if (completionListener != null) {
            completionListener = null;
        }
        if (textureListener != null) {
            textureListener = null;
        }
    }

    public void setModel(int model) {
        this.model = model;
    }



    @Override
    public void onPause() {
        if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                status = PAUSE;
            }
        }
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        reset(false);
        super.onDestroyView();
    }
}
