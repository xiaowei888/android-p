package com.magcomm.factorytest.item;

import android.content.Context;
import android.graphics.Color;
import android.os.SystemClock;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/27.
 */

public class UIMFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private LinearLayout llUIMGroup;
    private TelephonyManager telephonyManager;

    @Override
    protected int getCurrentView() {
        return R.layout.uim_test;
    }

    @Override
    protected void onFragmentCreat() {
        initView();
        initUIM();
    }

    private void initView() {
        llUIMGroup = (LinearLayout) view.findViewById(R.id.ll_uim_group);
    }

    private void initUIM() {
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);


        int mNumSlots = telephonyManager.getSimCount();
        SubscriptionManager subscriptionManager = SubscriptionManager.from(getActivity());
        for (int i = 0; i < mNumSlots; ++i) {
            final SubscriptionInfo subscriptionInfo = subscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
            if (subscriptionInfo != null) {
                //SIM name
                TextView tvSim = new TextView(context);
                tvSim.setTextSize(20);
                tvSim.setTextColor(Color.BLACK);
                tvSim.setText("SIM" + (i + 1) + ":" + subscriptionInfo.getCarrierName());
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.topMargin = 25;
                llUIMGroup.addView(tvSim, layoutParams);
                //SIM state
                TextView tvState = new TextView(context);
                tvState.setTextSize(20);
                tvState.setTextColor(Color.BLACK);
                tvState.setText(getSimStateName(telephonyManager.getSimState(i)));
                llUIMGroup.addView(tvState);
                //SIM network
                TextView tvNetWork = new TextView(context);
                tvNetWork.setTextSize(20);
                tvNetWork.setTextColor(Color.BLACK);
                tvNetWork.setText(context.getResources().getString(R.string.test_sim_network) + getNetworkTypeName(telephonyManager.getNetworkType(i)));
                llUIMGroup.addView(tvNetWork);

                if (Config.AUTO_TEST.equals(getMode())) {
                    if (tvState.getText().equals(getString(R.string.test_sim_status_0)) || tvState.getText().equals(getString(R.string.test_sim_status_1))) {
                        updateDataBase(Config.FAIL);
                        handler.obtainMessage(2).sendToTarget();
                    } else {
                        updateDataBase(Config.SUCCESS);
                        handler.obtainMessage(2).sendToTarget();
                    }
                    return;
                }
            }
        }

        if (llUIMGroup.getChildCount() == 0) {
            TextView notice = new TextView(context);
            notice.setTextSize(30);
            notice.setTextColor(Color.BLACK);
            notice.setText("No SIM");
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER;
            llUIMGroup.addView(notice, layoutParams);
            if (Config.AUTO_TEST.equals(getMode())) {
                updateDataBase(Config.FAIL);
                handler.obtainMessage(2).sendToTarget();
            }
        }
    }

    private String getSimStateName(int simstate) {
        switch (simstate) {
            case TelephonyManager.SIM_STATE_UNKNOWN:
                return getString(R.string.test_sim_status_0);
            case TelephonyManager.SIM_STATE_ABSENT:
                return getString(R.string.test_sim_status_1);
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return getString(R.string.test_sim_status_2);
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return getString(R.string.test_sim_status_3);
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return getString(R.string.test_sim_status_4);
            case TelephonyManager.SIM_STATE_READY:
                return getString(R.string.test_sim_status_5);
            default:
                return "";
        }
    }

    private String getNetworkTypeName(int netType) {
        switch (netType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "CDMA";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "CDMA - EvDo rev. 0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "CDMA - EvDo rev. A";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "CDMA - EvDo rev. B";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "CDMA - 1xRTT";
            default:
                return "UNKNOWN";
        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        if (Config.AUTO_TEST.equals(getMode())) {
            SystemClock.sleep(2000);
        }
    }
}
