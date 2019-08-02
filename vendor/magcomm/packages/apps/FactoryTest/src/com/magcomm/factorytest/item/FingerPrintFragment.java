package com.magcomm.factorytest.item;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;
import java.io.File;

/**
 * Created by zhangziran on 2017/12/29.
 */

public class FingerPrintFragment extends BaseFragment {
    private TextView tvHint;
    private int nums = 0;

    @Override
    protected int getCurrentView() {
        return R.layout.radio_test;
    }

    @Override
    protected void onFragmentCreat() {
        if (Config.AUTO_TEST.equals(getMode())) {
            setLLResultVisibility(true);
        }
        tvHint = (TextView) view.findViewById(R.id.tv_radio_hint);
        tvHint.setText(getResources().getString(R.string.fingerprint_hint));
        handler.postDelayed(runnable, 2000);
    }

    @Override
    public void onResume() {
        super.onResume();
        nums++;
        if (nums == 96) {
            nums = 0;
        }
        if (nums % 2 == 0) {
            tvHint.setText(getResources().getString(R.string.fingerprint_test_finish));
        }
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
		        String path1 = "/dev/sunwave_fp";
		        File file1 = new File(path1);
		        String path2 = "/dev/madev0";
		        File file2 = new File(path2);
		        String path3 = "/dev/blfp";
		        File file3 = new File(path3);
		        Log.i("zhangya", "checkFile: file1.exists()=" + file1.exists() + ", file2.exists()=" + file2.exists() + ", file3.exists()=" + file3.exists());
                ComponentName componentName = null;
		        if (file1.exists()) {
                    componentName = new ComponentName("com.swfp.factory", "com.swfp.activity.DetectActivity");				
		        } else if (file2.exists()) {
                    componentName = new ComponentName("ma.android.com.mafactory", "ma.android.com.mafactory.MainActivity");
				} else if (file3.exists()) {
                    componentName = new ComponentName("com.android.mmi", "com.android.mmi.MmiActivity");
                }
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setComponent(componentName);
                startActivity(intent);
            } catch (Exception e) {
                if (Config.AUTO_TEST.equals(getMode())) {
                    updateDataBase(Config.FAIL);
                    handler.obtainMessage(2).sendToTarget();
                } else if (Config.MANUAL_TEST.equals(getMode())) {
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
        runnable = null;
    }
}
