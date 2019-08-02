package com.magcomm.factorytest.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.magcomm.factorytest.FactoryTestApplication;
import com.magcomm.factorytest.R;
import com.magcomm.factorytest.batterytest.activity.BatteryTestActivity;
import com.magcomm.factorytest.util.Config;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "zhangziran";
    private Button btnAutoTest, btnManualTest, btnTestReport, btnVersion, btnBatteryTest, btnReset;
    private AlertDialog alertDialog;
    private AlertDialog.Builder builder;
    private long firstClick;
    private long secondClick;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onResume() {
        StatusBarManager statusBarManager = (StatusBarManager) getSystemService(Context.STATUS_BAR_SERVICE);
        statusBarManager.disable(StatusBarManager.DISABLE_EXPAND);
        super.onResume();
    }

    private void initView() {
        btnAutoTest = (Button) findViewById(R.id.btn_auto_test);
        btnAutoTest.setOnClickListener(this);
        btnManualTest = (Button) findViewById(R.id.btn_manual_test);
        btnManualTest.setOnClickListener(this);
        btnTestReport = (Button) findViewById(R.id.btn_test_report);
        btnTestReport.setOnClickListener(this);
        btnVersion = (Button) findViewById(R.id.btn_version);
        btnVersion.setOnClickListener(this);
        btnBatteryTest = (Button) findViewById(R.id.btn_batterytest);
        btnBatteryTest.setOnClickListener(this);
        btnReset = (Button) findViewById(R.id.btn_reset);
        btnReset.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        firstClick = System.currentTimeMillis();
        if(firstClick - secondClick < 1500) return;
        switch (v.getId()) {
            case R.id.btn_auto_test:
                Intent intent = new Intent(MainActivity.this, ItemTestActivity.class);
                intent.putExtra(Config.TEST_KEY, Config.AUTO_TEST);
                startActivity(intent);
                break;
            case R.id.btn_manual_test:
                intent = new Intent(MainActivity.this, BaseActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_test_report:
                intent = new Intent(MainActivity.this, TestResultActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_version:
                intent = new Intent(MainActivity.this, ItemTestActivity.class);
                intent.putExtra(Config.TEST_KEY, Config.VERSION_TEST);
                startActivity(intent);
                break;
            case R.id.btn_batterytest:
                intent = new Intent(MainActivity.this, BatteryTestActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_reset:
                reset();
                break;
        }
        secondClick = firstClick;
    }

    private void reset() {
        builder = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.factory_reset))
                .setMessage(getResources().getString(R.string.need_reset))
                .setNegativeButton(getResources().getString(R.string.reset_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDialog.dismiss();
                    }
                })
                .setPositiveButton(getResources().getString(R.string.reset_ensure), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
                        intent.setPackage("android");
                        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                        intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
                        //intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, true);
                        //intent.putExtra(Intent.EXTRA_WIPE_ESIMS, true);
                        sendBroadcast(intent);
                        alertDialog.dismiss();
                    }
                });
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown: keyCode=" + keyCode);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MENU:
                return true;
            case KeyEvent.KEYCODE_BACK:
                for (Activity activity : FactoryTestApplication.getStacks()) {
                    if (activity != null) {
                        Log.i(TAG, "onKeyDown: getLocalClassName=" + activity.getLocalClassName());
                        activity.finish();
                    }
                }
                this.finish();
                System.exit(0);
        }
        return super.onKeyDown(keyCode, event);
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
        FactoryTestApplication.getStacks().clear();
        FactoryTestApplication.getClasss().clear();
    }
}
