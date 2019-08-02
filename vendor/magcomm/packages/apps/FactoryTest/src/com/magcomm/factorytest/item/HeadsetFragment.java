package com.magcomm.factorytest.item;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;
import com.magcomm.factorytest.view.MikeView;
import com.magcomm.factorytest.view.VerticalSeekBar;

/**
 * Created by zhangziran on 2017/12/26.
 */

public class HeadsetFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private VerticalSeekBar leftSeekBar, rightSeekBar;
    private MikeView mikeView;
    private TextView tvError;
    private AudioManager audioManager;
    private boolean isHeadSetOn = false;

    private int recBufSize = 0, playBufSize = 0;
    private static final int SAMPLE_RATE_IN_HZ = 8000;
    private static final int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private static final int mSourceType = MediaRecorder.AudioSource.MIC;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private int mAudioBufferSimpleSize;
    private short[] audioBuffer;
    private int mVolume;
    private int j;
    private boolean isRun = false;
    private boolean isClosing = false;
    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;
    private BroadcastReceiver receiver;
    private float currentVolume, maxVolume, leftVolume, rightVolume;
    private RecordThread mRecordThread;
    private static boolean isSuccess = false;
    private boolean isInIt = false;
    private Runnable mUpdataResults = new Runnable() {
        public void run() {
            updateUI();
        }
    };

    @Override
    protected int getCurrentView() {
        return R.layout.headset_test;
    }

    @Override
    protected void onFragmentCreat() {
        leftSeekBar = (VerticalSeekBar) view.findViewById(R.id.vs_left_headset);
        rightSeekBar = (VerticalSeekBar) view.findViewById(R.id.vs_right_headset);
        mikeView = (MikeView) view.findViewById(R.id.mv_headset);
        tvError = (TextView) view.findViewById(R.id.tv_headset_error);
        tvError.setText(getResources().getString(R.string.headset_is_unavailable));
        mikeView.setVolume(0);
        mikeView.invalidate();
        mRecordThread = new RecordThread();
        initHeadset();
        registerReceiver();
    }

    private void registerReceiver() {
        receiver = new HeadsetReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        context.registerReceiver(receiver, filter);
    }

    private class HeadsetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                int state = intent.getIntExtra("state", 0);
                if (state == 0) {
                    setVisibility(0);
                } else if (state == 1) {
                    if(isInIt) {
                        isInIt = false;
                        return;
                    }
                    setVisibility(1);
                }
            }
        }
    }


    private void initHeadset() {
        initAudioTrack();
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        try {
            if (audioManager != null) {
                if (audioManager.isSpeakerphoneOn()) {
                    audioManager.setSpeakerphoneOn(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        leftVolume = rightVolume = currentVolume;
        leftSeekBar.setProgress(currentVolume / maxVolume);
        rightSeekBar.setProgress(currentVolume / maxVolume);
        AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_ALL);
        if (devices != null) {
            for (AudioDeviceInfo deviceInfo : devices) {
                if (AudioDeviceInfo.TYPE_WIRED_HEADSET == deviceInfo.getType()) {
                    isHeadSetOn = true;
                }
            }
        }
        if (isHeadSetOn) {
            leftSeekBar.setOnProgressChangeListener(new VerticalSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChangeListener(float progress) {
                    leftVolume = maxVolume * progress;
                    mAudioTrack.setStereoVolume(leftVolume, rightVolume);
                }
            });
            rightSeekBar.setOnProgressChangeListener(new VerticalSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChangeListener(float progress) {
                    rightVolume = maxVolume * progress;
                    mAudioTrack.setStereoVolume(leftVolume, rightVolume);
                }
            });
            isInIt = true;
            setVisibility(1);
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.postDelayed(successRunnable, 2000);
            }
        } else {
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.obtainMessage(2).sendToTarget();
                updateDataBase(Config.FAIL);
            }
            setVisibility(0);
        }
    }


    private void initAudioTrack() {
        playBufSize = 2 * AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, channelConfiguration, audioEncoding);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE_IN_HZ, channelConfiguration, audioFormat,
                playBufSize, AudioTrack.MODE_STREAM);
        int audioTrackState = mAudioTrack.getState();
        if (audioTrackState != AudioTrack.STATE_INITIALIZED) {
            Toast.makeText(getActivity(), "Mike open error", Toast.LENGTH_LONG).show();
            destroy();
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.obtainMessage(2).sendToTarget();
                updateDataBase(Config.FAIL);
            }
        }
        initAudioRecord();
    }

    private void initAudioRecord() {
        recBufSize = 2 * AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ, channelConfiguration, audioEncoding);
        mAudioBufferSimpleSize = recBufSize / 2;
        mAudioRecord = new AudioRecord(mSourceType, SAMPLE_RATE_IN_HZ, channelConfiguration, audioFormat, recBufSize);
        int audioRecordState = mAudioRecord.getState();
        if (audioRecordState != AudioRecord.STATE_INITIALIZED) {
            Toast.makeText(getActivity(), "Mike open error", Toast.LENGTH_LONG).show();
            destroy();
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.obtainMessage(2).sendToTarget();
                updateDataBase(Config.FAIL);
            }
        }
    }

    private void setVisibility(int mode) {
        switch (mode) {
            case 0:
                leftSeekBar.setVisibility(View.GONE);
                rightSeekBar.setVisibility(View.GONE);
                mikeView.setVisibility(View.GONE);
                tvError.setVisibility(View.VISIBLE);
                stop();
                break;
            case 1:
                leftSeekBar.setVisibility(View.VISIBLE);
                rightSeekBar.setVisibility(View.VISIBLE);
                mikeView.setVisibility(View.VISIBLE);
                tvError.setVisibility(View.GONE);
                start();
                break;
        }
    }

    private class RecordThread extends Thread {
        public RecordThread() {
            super();
        }

        public void run() {
            super.run();
            boolean interrupt = Thread.currentThread().isInterrupted();
            if (interrupt || mAudioRecord == null) {//modified by Yar @20190103
                return;
            }
            audioBuffer = new short[mAudioBufferSimpleSize];
            //modified by Yar @20190103
            while (true) {
                mAudioRecord.startRecording();
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    android.util.Log.i("Yar", "0. interrupt e -> " + e);
                    return;
                }
                int audioRecordState = mAudioRecord.getRecordingState();
                if (audioRecordState == AudioRecord.RECORDSTATE_RECORDING) {
                    //android.util.Log.i("Yar", "success");
                    break;
                } else {
                    android.util.Log.i("Yar", "record start error");
                }
            }
            //modified by Yar @20190103

            if (interrupt || mAudioTrack == null) {//modified by Yar @20190103
                return;
            }
            mAudioTrack.play();
            int audioTracktate = mAudioTrack.getPlayState();
            if (interrupt || audioTracktate != AudioTrack.PLAYSTATE_PLAYING) {
                return;
            }
            mAudioTrack.setStereoVolume(0.3f, 0.3f);
            isRun = true;
            while (!interrupt && isRun) {
                int sampleRate = mAudioRecord.read(audioBuffer, 0, mAudioBufferSimpleSize);
                if (mAudioBufferSimpleSize < 0) {
                    mAudioBufferSimpleSize = 0;
                }
                if ((mAudioTrack != null) && (!isClosing)) {
                    mAudioTrack.write(audioBuffer, 0, mAudioBufferSimpleSize);
                }

                int v = 0;
                for (int i = 0; i < audioBuffer.length; i++) {
                    v += audioBuffer[i] * audioBuffer[i];
                }
                if (sampleRate < 0) {
                    sampleRate = 0;
                }
                mVolume = v / (50 * (sampleRate + 1));
                j++;
                if (j < 5) {
                    mVolume = 20;
                }
                handler.post(mUpdataResults);
            }
            android.util.Log.i("Yar", "Headset Record end");
        }
    }


    private void updateUI() {
        mikeView.setVolume(mVolume);
        isSuccess = true;
    }

    private Runnable successRunnable = new Runnable() {
        @Override
        public void run() {
            if (isSuccess) {
                if (Config.AUTO_TEST.equals(getMode())) {
                    isSuccess = false;
                    handler.removeCallbacks(successRunnable);
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.SUCCESS);
                    return;
                }
            }
            handler.postDelayed(this, 2000);
        }
    };

    private void start() {
        isHeadSetOn = true;
        isClosing = false;
        if(mRecordThread !=null) {
            mRecordThread.interrupt();
            mRecordThread = null;
        }
        mRecordThread = new RecordThread();
        mRecordThread.start();
    }

    private void stop() {
        isSuccess = false;
        isHeadSetOn = false;
        isRun = false;
        isClosing = true;
        handler.removeCallbacks(mUpdataResults);
        successRunnable = null;
        if (mRecordThread != null) {
            mRecordThread.interrupt();
            mRecordThread = null;
        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        stop();
        leftSeekBar.removeProgressChangeListener();
        rightSeekBar.removeProgressChangeListener();
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
        if (mRecordThread != null) {
            mRecordThread.interrupt();
            mRecordThread = null;
        }
        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) currentVolume, 0);
        }
        if (mAudioTrack != null) {
            mAudioTrack.release();
        }
        if (mAudioRecord != null) {
            mAudioRecord.release();
        }
    }

    @Override
    public void onDestroyView() {
        if (tvError.getText().toString().equals(context.getResources().getString(R.string.headset_is_unavailable))) {
            SystemClock.sleep(2000);
        }
        super.onDestroyView();
    }
}
