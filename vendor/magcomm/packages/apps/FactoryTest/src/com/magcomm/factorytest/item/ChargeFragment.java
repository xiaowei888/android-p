package com.magcomm.factorytest.item;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;

/**
 * Created by zhangziran on 2017/12/26.
 */

public class ChargeFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private TextView tvBatteryState, tvBatteryV, tvPhoneTemperature, tvChargeA, tvChargeV, tvChargeNotice;
    private LinearLayout llChargeA, llChargeV;
    private BroadcastReceiver receiver;
    private static final String BATTERY_ACTION = Intent.ACTION_BATTERY_CHANGED;
    private static final String CHARGE_VOLTAGE_PATH = "/sys/class/power_supply/battery/ChargerVoltage";
    private static final String CHARGE_CURRENT_PATH = "/sys/class/power_supply/battery/BatteryAverageCurrent";
    private boolean isAttach = true;

    @Override
    protected int getCurrentView() {
        return R.layout.charge_test;
    }

    @Override
    protected void onFragmentCreat() {
        initView();
        initReceiver();
        handler.post(runnable);
    }

    private void initView() {
        tvBatteryState = (TextView) view.findViewById(R.id.tv_battery_state);
        tvBatteryV = (TextView) view.findViewById(R.id.tv_battery_voltage);
        tvPhoneTemperature = (TextView) view.findViewById(R.id.tv_current_temperature);
        tvChargeA = (TextView) view.findViewById(R.id.tv_charge_current);
        tvChargeV = (TextView) view.findViewById(R.id.tv_charge_voltage);
        tvChargeNotice = (TextView) view.findViewById(R.id.tv_charge_notice);
        llChargeA = (LinearLayout) view.findViewById(R.id.ll_charge_current);
        llChargeV = (LinearLayout) view.findViewById(R.id.ll_charge_voltage);
        tvChargeNotice.setText(getResources().getString(R.string.charge_notice));
        setVisibility(3);
    }


    private void initReceiver() {
        receiver = new BatteryReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BATTERY_ACTION);
        context.registerReceiver(receiver, filter);
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "onReceive: " + isAttach);
            if (BATTERY_ACTION.equals(action) && isAttach) {
                int batteryState = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -999);
                int plugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -999);
                int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -999);
                int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -999);
                String state = "";
                if (batteryState == 2) {
                    if (plugType > 0) {
                        if (plugType == 1) {
                            state = getResources().getString(R.string.battery_charge_state_ac);
                        } else {
                            state = getResources().getString(R.string.battery_charge_state_usb);
                        }
                        setVisibility(1);
                    }
                } else if (batteryState == 3) {
                    state = getResources().getString(R.string.battery_charge_state_discharging);
                    setVisibility(3);
                } else if (batteryState == 4) {
                    state = getResources().getString(R.string.battery_charge_state_notcharging);
                    setVisibility(0);
                } else if (batteryState == 5) {
                    state = getResources().getString(R.string.battery_charge_state_full);
                    setVisibility(1);
                } else {
                    state = getResources().getString(R.string.battery_charge_state_unknow);
                    setVisibility(2);
                }
                tvBatteryState.setText(state);
                if (voltage != -999) {
                    tvBatteryV.setText(voltage + "mV");
                }
                if (temperature != -999) {
                    tvPhoneTemperature.setText((temperature / 10) + "°C");
                }
            }
        }
    }


    private void setVisibility(int mode) {
        if (mode == 0) {
            llChargeA.setVisibility(View.GONE);
            llChargeV.setVisibility(View.GONE);
            tvChargeNotice.setVisibility(View.VISIBLE);
        } else if (mode == 1) {
            llChargeA.setVisibility(View.VISIBLE);
            llChargeV.setVisibility(View.VISIBLE);
            tvChargeNotice.setVisibility(View.GONE);
        } else if (mode == 2) {
            llChargeA.setVisibility(View.GONE);
            llChargeV.setVisibility(View.GONE);
            tvChargeNotice.setVisibility(View.GONE);
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getCurrent();
            if (isAttach) {
                handler.postDelayed(runnable, 2000);
            }
        }
    };

    private void getCurrent() {
        try {
            String chargeCurrent = "";
            String chargeVoltage = "";
            Class systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getDeclaredMethod("get", String.class);
            String platName = (String) get.invoke(null, "ro.hardware");
            if (platName.startsWith("mt") || platName.startsWith("MT")) {
                String filePath = "/sys/class/power_supply/battery/device/FG_Battery_CurrentConsumption";
                chargeCurrent = Math.round(getMeanCurrentVal(CHARGE_CURRENT_PATH, 5, 0)) + "mA";
                chargeVoltage = readFile(CHARGE_VOLTAGE_PATH, 0) + "mV";
            } else if (platName.startsWith("qcom")) {
                String filePath = "/sys/class/power_supply/battery/current_now";
                int current = Math.round(getMeanCurrentVal(filePath, 5, 0));
                int voltage = readFile("/sys/class/power_supply/battery/voltage_now", 0) / 1000;
                if (current < 0) {
                    chargeCurrent = (-current) + "mA";
                } else {
                    String discharge = current + "mA";
                }
                chargeVoltage = voltage + "mV";
            }
            tvChargeA.setText(chargeCurrent);
            tvChargeV.setText(chargeVoltage);
            if (Config.AUTO_TEST.equals(getMode())) {
                if (chargeCurrent.equals("0") || chargeVoltage.equals("0")) {
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.FAIL);
                } else {
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.SUCCESS);
                }
            }
        } catch (Exception e) {
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.obtainMessage(2).sendToTarget();
                updateDataBase(Config.FAIL);
            }
            e.printStackTrace();
        }
    }

    private float getMeanCurrentVal(String filePath, int totalCount, int intervalMs) {
        float meanVal = 0.0f;
        if (totalCount <= 0) {
            return 0.0f;
        }
        for (int i = 0; i < totalCount; i++) {
            try {
                float f = Float.valueOf(readFile(filePath, 0));
                meanVal += f / totalCount;
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (intervalMs <= 0) {
                continue;
            }
            try {
                Thread.sleep(intervalMs);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return meanVal;
    }

    private int readFile(String path, int defaultValue) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            int i = Integer.parseInt(bufferedReader.readLine(), 10);
            bufferedReader.close();
            return i;
        } catch (Exception localException) {
            localException.toString();
        }
        return defaultValue;
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
        isAttach = true;
        if (receiver != null) {
            context.unregisterReceiver(receiver);
        }
    }

    @Override
    public void onDestroyView() {
        if (Config.AUTO_TEST.equals(getMode())) {
            SystemClock.sleep(2000);
        }
        super.onDestroyView();
    }
}
