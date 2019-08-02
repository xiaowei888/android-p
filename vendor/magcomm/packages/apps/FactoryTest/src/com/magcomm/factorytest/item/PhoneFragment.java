package com.magcomm.factorytest.item;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.SystemClock;
import android.widget.TextView;
import android.net.Uri;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by zhangziran on 2018/1/16.
 */

public class PhoneFragment extends BaseFragment {
    private static final String TAG = "zhangziran";

	private TextView phone_state;

    @Override
    protected int getCurrentView() {
        return R.layout.phone_test;
    }

    @SuppressLint("ServiceCast")
    @Override
    protected void onFragmentCreat() {
	phone_state = (TextView)view.findViewById(R.id.phone_state);
	phone_state.setText("112");
        callPhone("112");
    }

    public void callPhone(String phoneNum){
        Intent intent = new Intent(Intent.ACTION_CALL_EMERGENCY);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        context.startActivity(intent);
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
