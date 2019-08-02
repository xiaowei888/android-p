package com.magcomm.factorytest.item;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/27.
 */

public class RadioFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private TextView tvRadioHint;
    private static int nums = 0;

    @Override
    protected int getCurrentView() {
        return R.layout.radio_test;
    }

    @Override
    protected void onFragmentCreat() {
        if (Config.AUTO_TEST.equals(getMode())) {
            setLLResultVisibility(true);
        }
        tvRadioHint = (TextView) view.findViewById(R.id.tv_radio_hint);
        tvRadioHint.setText(getResources().getString(R.string.radio_hint));
        handler.postDelayed(runnable, 2000);
    }


    @Override
    public void onResume() {
        super.onResume();
        nums++;
        Log.i(TAG, "onResume: nums="+nums);
        if (nums == 96) {
            nums = 0;
        }
        if(nums % 2 == 0) {
            tvRadioHint.setText(getResources().getString(R.string.fingerprint_test_finish));
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                ComponentName componentName = new ComponentName("com.android.fmradio", "com.android.fmradio.FmMainActivity");
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                if (Config.AUTO_TEST.equals(getMode())) {
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.FAIL);
                } else if (Config.MANUAL_TEST.equals(getMode())) {
                    Log.i(TAG, "run: handler" + getTotalName());
                    handler.obtainMessage(0).sendToTarget();
                }
            }
        }
    };

    @Override
    public void onClick(String result) {
        if (Config.AUTO_TEST.equals(getMode())) {
            setLLResultVisibility(false);
        }
        updateDataBase(result);
        handler.obtainMessage(2).sendToTarget();
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        handler.removeCallbacks(runnable);
        this.runnable = null;
        nums = 0;
    }
}
