package com.magcomm.factorytest.batterytest.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.batterytest.activity.BatteryBaseActivity;
import com.magcomm.factorytest.view.MikeView;


/**
 * Created by zhangziran on 2017/12/1.
 */


public class MikeTestFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private View view;
    private RelativeLayout relativeLayout;
    private MikeView mikeView;
    private boolean isRun = false;
    private boolean isClosing = false;
    private AudioManager mAudioManager;
    private RecordThread mRecordThread;
    private short[] audioBuffer;
    private int state = 0;
    private int mVolume = 0;
    private int nCurrentMusicVolume;
    private boolean flag = false;

    private static final int mSourceType = MediaRecorder.AudioSource.MIC;

    private static final int SAMPLE_RATE_IN_HZ = 8000;

    private static final int channelConfiguration = AudioFormat.CHANNEL_IN_STEREO;
    private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    private int recBufSize = 0, playBufSize = 0;
    private int mAudioBufferSimpleSize;
    private AudioRecord mAudioRecord;
    private AudioTrack mAudioTrack;
    private int j;
    private int sampleRate = 8000;
    private int channelConfig = AudioFormat.CHANNEL_IN_MONO;
    private int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
    private long startTime = 0L;
    private long currentTime = 0L;
    private Handler handler;
    private final Runnable mUpdataResults = new Runnable() {
        public void run() {
            updateUI();
        }
    };

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
        view = inflater.inflate(R.layout.mike_test, null);
        relativeLayout = (RelativeLayout)view.findViewById(R.id.rl_mike_contain);
        initView();
        initAudio();
        startTime = System.currentTimeMillis();
        return view;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initView() {
        mikeView = new MikeView(getActivity());
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        float proportion = 1f;
        if (displayWidth > 700) {
            proportion = 1.5f;
        } else if (displayWidth > 1000) {
            proportion = 2f;
        } else if (displayWidth > 1300) {
            proportion = 2.5f;
        }
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams((int) (300 * proportion), (int) (120 * proportion));
        relativeLayout.addView(mikeView, layoutParams);
    }

    private void initAudio() {
        mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        getActivity().setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        nCurrentMusicVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        try {
            if (mAudioManager != null) {
                if (mAudioManager.isSpeakerphoneOn()) {
                    mAudioManager.setSpeakerphoneOn(false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
        initAudioTrack();
        mikeView.setVolume(0);
        mikeView.invalidate();
        mRecordThread = new RecordThread();
        mRecordThread.start();
    }

    private void initAudioTrack() {
        playBufSize = 2 * AudioTrack.getMinBufferSize(SAMPLE_RATE_IN_HZ, channelConfiguration, audioEncoding);
        mAudioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
                SAMPLE_RATE_IN_HZ, channelConfiguration, audioFormat,
                playBufSize, AudioTrack.MODE_STREAM);
        int audioTrackState = mAudioTrack.getState();
        if (audioTrackState != AudioTrack.STATE_INITIALIZED) {
            Toast.makeText(getActivity(), "Mike open error", Toast.LENGTH_LONG).show();
            reset(false);
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
            reset(false);
        }
    }

    private class RecordThread extends Thread {
        public RecordThread() {
            super();
        }

        public void run() {
            super.run();
            if (mAudioRecord == null) {
                return;
            }
            audioBuffer = new short[mAudioBufferSimpleSize];
            mAudioRecord.startRecording();
            int audioRecordState = mAudioRecord.getRecordingState();
            if (audioRecordState != AudioRecord.RECORDSTATE_RECORDING) {
                Log.i("zhangziran","not recording");
                return;
            } else {
                Log.i("zhangziran","start recording");
            }

            if (mAudioTrack == null) {
                return;
            }
            mAudioTrack.play();
            int audioTracktate = mAudioTrack.getPlayState();
            if (audioTracktate != AudioTrack.PLAYSTATE_PLAYING) {
                Log.i("zhangziran","not playing");
                return;
            }
            Log.i("zhangziran","start playing");
            mAudioTrack.setStereoVolume(0.3f, 0.3f);
            isRun = true;
            while (isRun) {
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
                Log.i("zhangziran","mVolume=" + mVolume);
                j++;
                if (j < 5) {
                    mVolume = 20;
                }
                handler.post(mUpdataResults);
            }
            //reset(false);
        }

        public void pause() {
            isRun = false;
        }

        public void start() {
            if (!isRun) {
                super.start();
            }
        }
    }

    private void releaseRec() {
        if (mAudioManager != null) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, nCurrentMusicVolume, 0);
        }
        if (mAudioTrack != null) {
            mAudioTrack.release();
        }
        if (mAudioRecord != null) {
            mAudioRecord.release();
        }
    }

    private void updateUI() {
        mikeView.setVolume(mVolume);
        if ((!flag) && (mVolume > 3000)) {
            handler.postDelayed(runnable, 5000);
            flag = true;
        }else {
            currentTime = System.currentTimeMillis();
            if((currentTime-startTime)>10000) {
                startTime = currentTime;
                handler.post(runnable);
            }
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            reset(false);
            handler.obtainMessage(0).sendToTarget();
        }
    };

    public void reset(boolean fail) {
        isRun = false;
        isClosing = true;
        flag =false;
        if (mRecordThread != null) {
            mRecordThread.interrupt();
            mRecordThread = null;
        }
        handler.removeCallbacks(mUpdataResults);
        handler.removeCallbacks(runnable);
        releaseRec();
    }

    @Override
    public void onDestroyView() {
        reset(false);
        super.onDestroyView();
    }
}

