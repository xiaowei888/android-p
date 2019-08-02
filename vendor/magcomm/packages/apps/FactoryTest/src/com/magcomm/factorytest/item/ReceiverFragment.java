package com.magcomm.factorytest.item;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Chronometer;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * Created by zhangziran on 2017/12/21.
 */

public class ReceiverFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private Chronometer chronometer;
    private DecimalFormat decimalFormat;
    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private AssetManager assetManager;
    private int currentInCallVolume;
    private MediaPlayer.OnCompletionListener completionListener;
    private AssetFileDescriptor assetFileDescriptor;

    @Override
    protected int getCurrentView() {
        return R.layout.receiver_test;
    }

    @Override
    protected void onFragmentCreat() {
        initView();
        initReceiver();
    }


    private void initView() {
        decimalFormat = new DecimalFormat("00");
        chronometer = (Chronometer) view.findViewById(R.id.receiver_timer);
        chronometer.setBase(SystemClock.elapsedRealtime());
        int hour = (int) (SystemClock.elapsedRealtime() - chronometer.getBase()) / 60000;
        String strHour = decimalFormat.format(hour);
        chronometer.setFormat(strHour + ":%s");
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void initReceiver() {
        try {
            if (audioManager == null) {
                audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            }
            if (assetManager == null) {
                assetManager = getActivity().getAssets();
            }
            if (mediaPlayer != null) {
                mediaPlayer.reset();
                mediaPlayer.release();
                mediaPlayer = null;
            }
            if (completionListener == null) {
                completionListener = new ReceiverCompletionListener();
            }
            mediaPlayer = new MediaPlayer();
            if (audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(false);
            }
            currentInCallVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            int volume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            assetFileDescriptor = assetManager.openFd("canon.mp3");
            mediaPlayer.setDataSource(assetFileDescriptor);
            mediaPlayer.prepare();
            if (Config.MANUAL_TEST.equals(getMode())) {
                mediaPlayer.setLooping(true);
            }
            mediaPlayer.start();
            chronometer.start();
            mediaPlayer.setOnCompletionListener(completionListener);
        } catch (IOException e) {
            if (Config.MANUAL_TEST.equals(getMode())) {
                Log.i(TAG, "initReceiver: handler" + getTotalName());
                handler.obtainMessage(0).sendToTarget();
            } else if (Config.AUTO_TEST.equals(getMode())) {
                handler.obtainMessage(2).sendToTarget();
                updateDataBase(Config.FAIL);
            }
            e.printStackTrace();
        }
    }

    private class ReceiverCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (Config.AUTO_TEST.equals(getMode())) {
                updateDataBase(Config.SUCCESS);
                handler.obtainMessage(2).sendToTarget();
            }
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
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (audioManager != null) {
            if (!audioManager.isSpeakerphoneOn()) {
                audioManager.setSpeakerphoneOn(true);
            }
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currentInCallVolume, 0);
        }
        chronometer.stop();
        if(assetFileDescriptor!=null) {
            try {
                assetFileDescriptor.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
