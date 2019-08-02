package com.magcomm.factorytest.batterytest.activity;

import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.magcomm.factorytest.FactoryTestApplication;
import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.SharepreferenceUtil;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by zhangziran on 18-3-9.
 */

public class BatteryTestActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener{
    private static final String TAG = "zhangziran";
    private static final String LOCKED = "lock";
    private CheckBox cVideoSpeaker, cVideoReceiver, cVibrator, cMikeReceiver,
            cFrontCreame, cBackCamera, cBackLight;
    private Spinner spinner;
    private Button btStart;
    private TextView tvResult;
    private ArrayList<Integer> ids;
    private ArrayList<Integer> idsBackups;
    private String[] times;
    private int testTime = 1;
    private ArrayList<CheckBox> checkBoxes;
    private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
        setContentView(R.layout.battery_test);
        FactoryTestApplication.getStacks().add(this);
        initView();
        init();
    }

    private void initView() {
        checkBoxes = new ArrayList<>();
        cVideoSpeaker = (CheckBox) findViewById(R.id.video_speaker);
        cVideoSpeaker.setOnClickListener(this);
        checkBoxes.add(cVideoSpeaker);
        cVideoReceiver = (CheckBox) findViewById(R.id.video_receiver);
        cVideoReceiver.setOnClickListener(this);
        checkBoxes.add(cVideoReceiver);
        cVibrator = (CheckBox) findViewById(R.id.vibrator);
        cVibrator.setOnClickListener(this);
        checkBoxes.add(cVibrator);
        cMikeReceiver = (CheckBox) findViewById(R.id.mike_receiver);
        cMikeReceiver.setOnClickListener(this);
        checkBoxes.add(cMikeReceiver);
        cFrontCreame = (CheckBox) findViewById(R.id.front_creame);
        cFrontCreame.setOnClickListener(this);
        checkBoxes.add(cFrontCreame);
        cBackCamera = (CheckBox) findViewById(R.id.back_camera);
        cBackCamera.setOnClickListener(this);
        checkBoxes.add(cBackCamera);
        cBackLight = (CheckBox) findViewById(R.id.back_light);
        cBackLight.setOnClickListener(this);
        checkBoxes.add(cBackLight);
        spinner = (Spinner) findViewById(R.id.time_spinner);
        spinner.setOnItemSelectedListener(this);
        btStart = (Button) findViewById(R.id.start);
        btStart.setOnClickListener(this);
        tvResult = (TextView) findViewById(R.id.test_result);
    }

    private void init() {
        ids = new ArrayList<>();
        times = getResources().getStringArray(R.array.spinner);
    }

    @Override
    protected void onResume() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        int result = SharepreferenceUtil.getTestResult();
        if (result == 0) {
            tvResult.setText(getResources().getString(R.string.test_fail));
        } else if (result == 1) {
            tvResult.setText(getResources().getString(R.string.test_success));
        } else if (result == 2) {
            tvResult.setText(getResources().getString(R.string.stop_test));
        } else {
            tvResult.setText("");
        }
        SharepreferenceUtil.setTestResult(4);
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.video_speaker:
                if (cVideoSpeaker.isChecked()) {
                    add(0);
                } else {
                    cVideoSpeaker.setChecked(false);
                    remove(0);
                }
                break;
            case R.id.video_receiver:
                if (cVideoReceiver.isChecked()) {
                    add(1);
                } else {
                    cVideoReceiver.setChecked(false);
                    remove(1);
                }
                break;
            case R.id.vibrator:
                if (cVibrator.isChecked()) {
                    add(2);
                } else {
                    cVibrator.setChecked(false);
                    remove(2);
                }

                break;
            case R.id.mike_receiver:
                if (cMikeReceiver.isChecked()) {
                    add(3);
                } else {
                    cMikeReceiver.setChecked(false);
                    remove(3);
                }
                break;
            case R.id.front_creame:
                if (cFrontCreame.isChecked()) {
                    add(4);
                } else {
                    cFrontCreame.setChecked(false);
                    remove(4);
                }
                break;
            case R.id.back_camera:
                if (cBackCamera.isChecked()) {
                    add(5);
                } else {
                    cBackCamera.setChecked(false);
                    remove(5);
                }
                break;
            case R.id.back_light:
                if (cBackLight.isChecked()) {
                    add(6);
                } else {
                    cBackLight.setChecked(false);
                    remove(6);
                }
                break;
            case R.id.start:
                if (ids.size() != 0) {
                    Collections.sort(ids);
                    Intent intent = new Intent(BatteryTestActivity.this, BatteryBaseActivity.class);
                    intent.putIntegerArrayListExtra("test_itme", ids);
                    intent.putExtra("test_time", testTime);
                    startActivityForResult(intent, REQUEST_CODE);
                    idsBackups = ids;
                    reset();
                } else {
                    Toast.makeText(BatteryTestActivity.this, getResources().getString(R.string.battery_test_notice), Toast.LENGTH_LONG).show();
                }
            default:
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        testTime = Integer.parseInt(times[position].substring(0, times[position].length() - 1));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void add(int id) {
        synchronized (LOCKED) {
            if (ids.size() == 0) {
                ids.add(id);
            } else {
                for (int i = 0; i < ids.size(); i++) {
                    if (ids.get(i) == id) {
                        return;
                    } else {
                        ids.add(id);
                        break;
                    }
                }
            }
        }
    }

    private void remove(int id) {
        if (ids.size() == 0) return;
        synchronized (LOCKED) {
            for (int i = 0; i < ids.size(); i++) {
                if (ids.get(i) == id) {
                    ids.remove(i);
                    break;
                }
            }
        }
    }

    private void reset() {
        ids.clear();
        for (int i = 0; i < checkBoxes.size(); i++) {
            checkBoxes.get(i).setChecked(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            switch (resultCode) {
                case 1:
                    tvResult.setText(getResources().getString(R.string.test_success));
                    break;
                case 2:
                    tvResult.setText(getResources().getString(R.string.stop_test));
                    break;
            }
        }
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
                finish();
                return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStop() {
        super.onStop();
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_NONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharepreferenceUtil.setTestResult(4);
        if (idsBackups != null) {
            idsBackups.clear();
        }
        reset();
    }

}
