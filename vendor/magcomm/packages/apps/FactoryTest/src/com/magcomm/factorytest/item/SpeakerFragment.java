package com.magcomm.factorytest.item;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.io.IOException;

/**
 * Created by zhangziran on 2017/12/27.
 */

public class SpeakerFragment extends BaseFragment {
    private AudioManager audioManager;
    private int currentMode = -999;
    private int currentVolume = -999;
    private int maxVolume;
    private MediaPlayer mediaPlayer;
    private MediaPlayer.OnCompletionListener completionListener;
    private AssetFileDescriptor fileDescriptor;

    @Override
    protected int getCurrentView() {
        return R.layout.speaker_test;
    }

    @Override
    protected void onFragmentCreat() {
        initSpeaker();
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void initSpeaker() {
        try {
            if (audioManager == null) {
                audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            }
            currentMode = audioManager.getMode();
            if (currentMode != AudioManager.MODE_CURRENT) {
                audioManager.setMode(AudioManager.MODE_NORMAL);
            }
            currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
            mediaPlayer = new MediaPlayer();
            AssetManager assetManager = context.getAssets();
            fileDescriptor = assetManager.openFd("speaker.mp3");
            mediaPlayer.reset();
            mediaPlayer.setDataSource(fileDescriptor);
            if (Config.MANUAL_TEST.equals(getMode())) {
                mediaPlayer.setLooping(true);
            } else if (Config.AUTO_TEST.equals(getMode())) {
                completionListener = new MediaCompletionListener();
                mediaPlayer.setOnCompletionListener(completionListener);
            }
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
            if (Config.MANUAL_TEST.equals(getMode())) {
                Log.i("zhangziran", "initSpeaker:handler " + getTotalName());
                handler.obtainMessage(0).sendToTarget();
            } else if (Config.AUTO_TEST.equals(getMode())) {
                handler.obtainMessage(2).sendToTarget();
                updateDataBase(Config.FAIL);
            }
        }
    }

    private class MediaCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            handler.obtainMessage(2).sendToTarget();
            updateDataBase(Config.SUCCESS);
        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.setOnCompletionListener(null);
            completionListener = null;
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (currentVolume != -999) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
        }
        if (currentMode != -999) {
            audioManager.setMode(currentMode);
        }
        if (fileDescriptor != null) {
            try {
                fileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
