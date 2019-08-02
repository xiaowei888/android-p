package com.magcomm.factorytest.batterytest.activity;

import android.annotation.TargetApi;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.magcomm.factorytest.FactoryTestApplication;
import com.magcomm.factorytest.R;
import com.magcomm.factorytest.batterytest.entity.FragmentEntity;
import com.magcomm.factorytest.batterytest.fragment.BaseFragment;
import com.magcomm.factorytest.batterytest.fragment.Camera1TestFragment;
import com.magcomm.factorytest.batterytest.fragment.FlashLightTestFragment;
import com.magcomm.factorytest.batterytest.fragment.MikeTestFragment;
import com.magcomm.factorytest.batterytest.fragment.VibrationTestFragment;
import com.magcomm.factorytest.batterytest.fragment.VideoTestFragment;
import com.magcomm.factorytest.util.SharepreferenceUtil;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by zhangziran on 18-3-9.
 */

public class BatteryBaseActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "zhangziran";
    private TextView tvLaveTime;
    private Button btStop;
    private final Handler handler = new BaseHandler();
    private ArrayList<Integer> ids;
    private int testTime;
    private ArrayList<FragmentEntity> fragments;
    private FragmentManager fragmentManager;
    private static int currentPosition = 0;
    private BatteryReceiver receiver;
    private long startTime = 0L;
    private long currentTime = 0L;
    private long totalTime = 0L;
    private boolean isRunning = true;
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
        setContentView(R.layout.activity_battery_base);
        FactoryTestApplication.getStacks().add(this);
        initView();
        initData();
        initTime();
        initReceiver();
        fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.frame_layout, fragments.get(0).getFragment());
        transaction.commit();
    }

    @Override
    protected void onResume() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        super.onResume();
    }

    private void initView() {
        tvLaveTime = (TextView) findViewById(R.id.lave_time);
        btStop = (Button) findViewById(R.id.stop);
        btStop.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.stop) {
            finishThis(2);
        }
    }

    private void initData() {
        Intent intent = getIntent();
        ids = intent.getIntegerArrayListExtra("test_itme");
        testTime = intent.getIntExtra("test_time", 1);
        fragments = new ArrayList<>();
        for (int id : ids) {
            Log.i(TAG, "initData: id=" + id);
            addFragment(id);
        }
    }

    private void addFragment(int id) {
        switch (id) {
            case 0:
                FragmentEntity videoSpeaker = new FragmentEntity(1, new VideoTestFragment());
                fragments.add(videoSpeaker);
                break;
            case 1:
                FragmentEntity videoReceiver = new FragmentEntity(0, new VideoTestFragment());
                fragments.add(videoReceiver);
                break;
            case 2:
                FragmentEntity vibration = new FragmentEntity(0, new VibrationTestFragment());
                fragments.add(vibration);
                break;
            case 3:
                FragmentEntity mikeReceiver = new FragmentEntity(0, new MikeTestFragment());
                fragments.add(mikeReceiver);
                break;
            case 4:
                FragmentEntity frontCamera = new FragmentEntity(1, new Camera1TestFragment());
                fragments.add(frontCamera);
                break;
            case 5:
                FragmentEntity backCamera = new FragmentEntity(0, new Camera1TestFragment());
                fragments.add(backCamera);
                break;
            case 6:
                FragmentEntity backLight = new FragmentEntity(0, new FlashLightTestFragment());
                fragments.add(backLight);
                break;
        }
    }

    public Handler getHandler() {
        return handler;
    }

    private class BaseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    onNext();
                    break;
            }
        }
    }

    private void onNext() {
        if (isRunning) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            if (fragments.size() == 1) {
                transaction.remove(fragments.get(0).getFragment());
                BaseFragment fragment = fragments.get(0).getFragment();
                int model = fragment.getModel();
                fragments.remove(0);
                if (fragment instanceof VideoTestFragment) {
                    fragments.add(new FragmentEntity(model, new VideoTestFragment()));
                } else if (fragment instanceof VibrationTestFragment) {
                    fragments.add(new FragmentEntity(model, new VibrationTestFragment()));
                } else if (fragment instanceof MikeTestFragment) {
                    fragments.add(new FragmentEntity(model, new MikeTestFragment()));
                } else if (fragment instanceof Camera1TestFragment) {
                    fragments.add(new FragmentEntity(model, new Camera1TestFragment()));
                } else if (fragment instanceof FlashLightTestFragment) {
                    fragments.add(new FragmentEntity(model, new FlashLightTestFragment()));
                }
                fragment.reset(false);
                fragment = null;
                transaction.add(R.id.frame_layout, fragments.get(currentPosition).getFragment());
            } else {
                fragments.get(currentPosition).getFragment().reset(false);
                currentPosition++;
                if (currentPosition >= fragments.size()) {
                    currentPosition = 0;
                }
                transaction.replace(R.id.frame_layout, fragments.get(currentPosition).getFragment());
            }
            transaction.commit();
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private void initTime() {
        Calendar calendar = Calendar.getInstance();
        int second = calendar.get(Calendar.SECOND);
        totalTime = testTime * 60 * 60 * 1000 - (60 - second);
        startTime = System.currentTimeMillis();
        String time = formatTime((int) (totalTime) / 1000);
        tvLaveTime.setText(time);
    }

    private void initReceiver() {
        receiver = new BatteryReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        registerReceiver(receiver, intentFilter);
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            currentTime = System.currentTimeMillis();
            long laveTime = totalTime - (currentTime - startTime);
            if (laveTime <= 0) {
                tvLaveTime.setText("00:00");
                finishThis(1);
            } else {
                tvLaveTime.setText(formatTime((int) laveTime / 1000));
            }
        }
    }

    private String formatTime(int laveTime) {
        String strMinute = getResources().getString(R.string.minute);
        String strHour = getResources().getString(R.string.hour);
        StringBuffer stringBuffer = new StringBuffer();
        if (laveTime >= 3600) {
            int hour = laveTime / 3600;
            int minute = (laveTime % 3600) / 60;
            stringBuffer.append(hour + strHour + minute + strMinute);
        } else if (laveTime >= 60 && laveTime < 3600) {
            int minute = laveTime / 60;
            stringBuffer.append(minute + strMinute);
        } else {
            stringBuffer.append(0 + strMinute);
        }
        return stringBuffer.toString();
    }


    private void reset() {
        isRunning = false;
        fragments.get(currentPosition).getFragment().reset(false);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        for (int i = 0; i < fragments.size(); i++) {
            transaction.remove(fragments.get(i).getFragment());
        }
        transaction.commit();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_ENDCALL:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_POWER:
            case KeyEvent.KEYCODE_CAMERA:
                return true;
            case KeyEvent.KEYCODE_BACK:
                finishThis(2);
                return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void finishThis(int code) {
        SharepreferenceUtil.setTestResult(code);
        reset();
        setResult(code);
        finish();
    }

    @Override
    protected void onStop() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_NONE);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        if (fragments.size() != 0) {
            fragments.clear();
            fragments = null;
        }
        isRunning = true;
        currentPosition = 0;
    }

    @Override
    public void onLowMemory() {
        System.gc();
        super.onLowMemory();
    }

}
