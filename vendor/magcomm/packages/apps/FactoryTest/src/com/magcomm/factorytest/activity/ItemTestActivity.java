package com.magcomm.factorytest.activity;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.magcomm.factorytest.FactoryTestApplication;
import com.magcomm.factorytest.R;
import com.magcomm.factorytest.entity.ClassEntity;
import com.magcomm.factorytest.entity.ItemEntity;
import com.magcomm.factorytest.item.BaseFragment;
import com.magcomm.factorytest.item.TPFragment;
import com.magcomm.factorytest.item.KeyboardFragment;
import com.magcomm.factorytest.item.VersionFragment;
import com.magcomm.factorytest.util.AppManager;
import com.magcomm.factorytest.util.Config;

import java.util.List;

public class ItemTestActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "zhangziran";
    private static BaseHandler handler;
    private static String testMode = "";
    private Button btnSuccess, btnFail;
    private LinearLayout llResult;
    private static FragmentManager fragmentManager;
    private List<ClassEntity> classEntities;
    private List<ItemEntity> itemEntities;
    private BaseFragment fragment;
    private OnTouchListener touchListener;
    private OnButtonClickListener buttonClickListener;
    private AppManager appManager;
    private static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;
    private static int position = 0;
    private String[] permission = new String[]{
            Manifest.permission.CAMERA, Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);
        FactoryTestApplication.getStacks().add(this);
        handler = new BaseHandler();
        fragmentManager = getFragmentManager();
        Intent intent = getIntent();
        String mode = testMode = intent.getStringExtra(Config.TEST_KEY);
        setContentView(R.layout.activity_item);
        if (Config.MANUAL_TEST.equals(mode)) {
            int position = intent.getIntExtra(Config.MANUAL_ITEM_KEY, -999);
            initManual(position);
            initView(mode);
        } else if (Config.AUTO_TEST.equals(mode)) {
            initAuto();
            initView(mode);
        } else if (Config.VERSION_TEST.equals(mode)) {
            initView(mode);
            setLLResultVisibility(false);
            fragment = new VersionFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.fragment_contain, fragment);
            fragmentTransaction.commit();
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, 0);
        }
    }

    @Override
    protected void onResume() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        super.onResume();
        //added by Yar begin
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, 1001);
        }
        //added by Yar end
    }


    private void initManual(int position) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        classEntities = FactoryTestApplication.getClasss();
        if (position != -999) {
            Class aClass = classEntities.get(position).getaClass();
            try {
                fragment = (BaseFragment) aClass.newInstance();
                fragment.setMode(Config.MANUAL_TEST);
                fragmentTransaction.add(R.id.fragment_contain, fragment);
                fragmentTransaction.commit();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void initView(String mode) {
        llResult = (LinearLayout) findViewById(R.id.ll_result);
        btnSuccess = (Button) findViewById(R.id.btn_success);
        btnSuccess.setOnClickListener(this);
        btnFail = (Button) findViewById(R.id.btn_fail);
        btnFail.setOnClickListener(this);
        switch (mode) {
            case Config.MANUAL_TEST:
                if (fragment instanceof TPFragment) {
                    llResult.setVisibility(View.GONE);
                }
                break;
            case Config.AUTO_TEST:
                llResult.setVisibility(View.GONE);
                break;
        }

    }

    public void setBtnSuccessEnable(boolean enable) {
        if (btnSuccess != null) {
            btnSuccess.setEnabled(enable);
        }
    }

    public void setBtnFailEnable(boolean enable) {
        if (btnFail != null) {
            btnFail.setEnabled(enable);
        }
    }

    public void setLLResultVisibility(boolean enable) {
        if (llResult != null) {
            llResult.setVisibility(enable ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_success:
                if (Config.AUTO_TEST.equals(testMode)) {
                    buttonClickListener.onClick(Config.SUCCESS);
                } else if (Config.MANUAL_TEST.equals(testMode)) {
                    setResult(Config.SUCCESS_RESULT);
                    destroy();
                }
                break;
            case R.id.btn_fail:
                if (Config.AUTO_TEST.equals(testMode)) {
                    buttonClickListener.onClick(Config.FAIL);
                } else if (Config.MANUAL_TEST.equals(testMode)) {
                    setResult(Config.FAIL_RESULT);
                    destroy();
                }
                break;
        }
    }

    private void initAuto() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        appManager = AppManager.getInstance(this);
        itemEntities = appManager.getItems();
        fragmentTransaction.add(R.id.fragment_contain, itemEntities.get(position).getFragment());
        fragmentTransaction.commit();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchListener.onDown((int) event.getRawX(), (int) event.getRawY());
                break;
            case MotionEvent.ACTION_MOVE:
                touchListener.onMove((int) event.getRawX(), (int) event.getRawY());
                break;
            case MotionEvent.ACTION_UP:
                touchListener.onUp((int) event.getRawX(), (int) event.getRawY());
                break;
        }
        return false;
    }

    private class BaseHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            //Log.i(TAG, "handleMessage:mag.what=: " + msg.what);
            switch (msg.what) {
                case 0:
                    setResult(Config.FAIL_RESULT);
                    destroy();
                    break;
                case 1:
                    setResult(Config.SUCCESS_RESULT);
                    destroy();
                    break;
                case 2:
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    itemEntities.get(position).getFragment().destroy();
                    position++;
                    if (position >= itemEntities.size()) {
                        Intent intent = new Intent(ItemTestActivity.this, TestResultActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        fragmentTransaction.replace(R.id.fragment_contain, itemEntities.get(position).getFragment());
                        fragmentTransaction.commit();
                    }
                    break;
            }
        }
    }

    private void destroy() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (fragment != null) {
            fragment.destroy();
            fragmentTransaction.remove(fragment);
            fragment = null;
        }
        finish();
    }

    public BaseHandler getHandler() {
        return handler;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_CALL:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_MUTE:
            case KeyEvent.KEYCODE_CAMERA:
                touchListener.onKeyDown(keyCode);
                return true;
            case KeyEvent.KEYCODE_BACK:
			    // magcomm songkun open back key except keyboardFragment 190301
                /*
                if (fragment instanceof TPFragment || (Config.VERSION_TEST.equals(testMode) && fragment instanceof VersionFragment)) {
                    touchListener.onKeyDown(KeyEvent.KEYCODE_BACK);
                    return super.onKeyDown(keyCode, event);
                } else {
                    touchListener.onKeyDown(KeyEvent.KEYCODE_BACK);
                    return true;
                }
                */
                if(fragment instanceof KeyboardFragment) {
                    touchListener.onKeyDown(KeyEvent.KEYCODE_BACK);
                    return true;
                } else {
                    touchListener.onKeyDown(KeyEvent.KEYCODE_BACK);
                    setResult(Config.NO_RESULT);
                    destroy();
                    return super.onKeyDown(keyCode, event);
                }
        }
        return super.onKeyDown(keyCode, event);
    }


    public interface OnTouchListener {
        void onDown(int x, int y);

        void onMove(int x, int y);

        void onUp(int x, int y);

        void onKeyDown(int keyCode);
    }

    public void setOnTouchListener(OnTouchListener touchListener) {
        this.touchListener = touchListener;
    }

    public interface OnButtonClickListener {
        void onClick(String result);
    }

    public void setOnButtonClickListener(OnButtonClickListener buttonClickListener) {
        this.buttonClickListener = buttonClickListener;
    }

    @Override
    protected void onStop() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_NONE);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        position = 0;
        testMode = "";
        super.onDestroy();
    }
}
