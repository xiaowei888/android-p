package com.magcomm.factorytest.item;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.util.List;

/**
 * Created by zhangziran on 2017/12/23.
 */

public class WlanFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private TextView tvWifi;
    private WifiManager wifiManager;
    private List<ScanResult> listWifi;
    private StringBuffer stringBuffer;
    private boolean isAttach = true;
    private int[] nums = new int[]{0, 0, 0};

    @Override
    protected int getCurrentView() {
        return R.layout.wlan_test;
    }

    @Override
    protected void onFragmentCreat() {
        tvWifi = (TextView) view.findViewById(R.id.tv_wifi);
        initWifi();
    }

    private void initWifi() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
        stringBuffer = new StringBuffer();
        openWifi();
        handler.postDelayed(runnable, 1000);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            int status = wifiManager.getWifiState();
            if (isAttach) {
                if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                    wifiManager.startScan();
                    listWifi = wifiManager.getScanResults();
                    if (listWifi == null) {
                        Log.i(TAG, "run: listWifi is null");
                    } else {
                        Log.i(TAG, "run: listWifi.size=" + listWifi.size());
                    }
                    if (listWifi != null && listWifi.size() > 0) {
                        if (listWifi.size() > 5) {
                            showText(5, getResources().getString(R.string.wifi_first_five));
                        } else {
                            showText(listWifi.size(), "");
                        }
                    } else {
                        tvWifi.setText(getResources().getString(R.string.wifi_is_scanning));
                        destroyByAuto(0);
                    }
                } else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING) {
                    tvWifi.setText(getResources().getString(R.string.wifi_is_opening));
                    destroyByAuto(1);
                } else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
                    tvWifi.setText(getResources().getString(R.string.wifi_is_closed));
                    destroyByAuto(2);
                }
                if (this != null) {
                    handler.postDelayed(this, 2000);
                }
            }
        }
    };

    private void showText(int nums, String firstFive) {
        if (stringBuffer != null) {
            stringBuffer.delete(0, stringBuffer.length());
            stringBuffer = null;
        }
        stringBuffer = new StringBuffer();
        for (int i = 0; i < nums; i++) {
            ScanResult scanResult = listWifi.get(i);
            stringBuffer.append("【" + scanResult.SSID + "】")
                    .append(getResources().getString(R.string.wifi_signal_intensity))
                    .append(":")
                    .append(scanResult.level)
                    .append("\n\n");
            tvWifi.setText(getResources().getString(R.string.wifi_title) + firstFive + ":\n\n" + stringBuffer.toString());
        }
        if (Config.AUTO_TEST.equals(getMode())) {
            destroy();
            updateDataBase(Config.SUCCESS);
            handler.obtainMessage(2).sendToTarget();
            return;
        }
    }

    private void openWifi() {
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
    }

    private void closeWifi() {
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
    }

    private void destroyByAuto(int mode) {
        if (Config.AUTO_TEST.equals(getMode())) {
            nums[mode]++;
            if (nums[0] == 10 || nums[1] == 5 || nums[2] == 5) {
                updateDataBase(Config.FAIL);
                handler.removeCallbacks(runnable);
                handler.obtainMessage(2).sendToTarget();
            }
        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onDetach() {
        isAttach = false;
        super.onDetach();
    }

    @Override
    public void destroy() {
        handler.removeCallbacks(runnable);
        this.runnable = null;
        closeWifi();
    }

    @Override
    public void onDestroyView() {
        if (Config.AUTO_TEST.equals(getMode())) {
            SystemClock.sleep(2000);
        }
        super.onDestroyView();
    }
}
