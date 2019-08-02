package com.magcomm.factorytest.item;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.io.File;
import java.util.Scanner;

/**
 * Created by zhangziran on 2018/1/18.
 */

public class HDMIFragment extends BaseFragment {
    private TextView tvHDMIState;
    private BroadcastReceiver receiver;
    private static final String ACTION = "android.intent.action.HDMI_PLUG";

    @Override
    protected int getCurrentView() {
        return R.layout.hdmi_test;
    }

    @Override
    protected void onFragmentCreat() {
        tvHDMIState = (TextView) view.findViewById(R.id.tv_hdmi_state);
        if (isHdmiSwitchSet()) {
            setBtnSuccessEnable(true);
            tvHDMIState.setText(getResources().getString(R.string.detect_hdmi));
            destroyAuto(Config.SUCCESS);
        } else {
            setBtnSuccessEnable(false);
            tvHDMIState.setText(getResources().getString(R.string.no_hdmi));
            destroyAuto(Config.FAIL);
        }
        receiver = new HDMIReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION);
        context.registerReceiver(receiver, filter);
    }

    private boolean isHdmiSwitchSet() {
        File switchFile = new File("/sys/devices/virtual/switch/hdmi/state");
        if (!switchFile.exists()) {
            switchFile = new File("/sys/class/switch/hdmi/state");
        }
        try {
            Scanner switchFileScanner = new Scanner(switchFile);
            int switchValue = switchFileScanner.nextInt();
            switchFileScanner.close();
            return switchValue > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private class HDMIReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION.equals(action)) {
                int state = intent.getIntExtra("state", 0);
                if (state == 1) {
                    setBtnSuccessEnable(true);
                    tvHDMIState.setText(getResources().getString(R.string.detect_hdmi));
                } else {
                    setBtnSuccessEnable(false);
                    tvHDMIState.setText(getResources().getString(R.string.no_hdmi));
                }
            }
        }
    }

    private void destroyAuto(String result) {
        if (Config.AUTO_TEST.equals(getMode())) {
            updateDataBase(result);
            handler.obtainMessage(2).sendToTarget();
        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
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
