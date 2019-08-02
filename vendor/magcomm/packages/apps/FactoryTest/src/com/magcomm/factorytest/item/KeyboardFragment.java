package com.magcomm.factorytest.item;

import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhangziran on 2017/12/26.
 */

public class KeyboardFragment extends BaseFragment {
    private LinearLayout llKeys;
    private String[] keys;
    private Map<Integer, View> integerViewMap;
    private static boolean[] tags = new boolean[]{false, false, false, false, false};

    @Override
    protected int getCurrentView() {
        return R.layout.keyboard_test;
    }

    @Override
    protected void onFragmentCreat() {
        llKeys = (LinearLayout) view.findViewById(R.id.ll_keyboard_keys);
        initKeyboard();
    }

    private void initKeyboard() {
        integerViewMap = new HashMap<>();
        keys = getResources().getStringArray(R.array.keyboard_item);
        for (int i = 0; i < keys.length; i++) {
            TextView tvKey = new TextView(context);
            tvKey.setText(keys[i]);
            tvKey.setTransformationMethod(null);
            tvKey.setTextColor(Color.BLACK);
            tvKey.setTextSize(24);
            tvKey.setPadding(16, 0, 0, 0);
            tvKey.setTag(0);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.gravity = Gravity.CENTER_VERTICAL;
            layoutParams.setMargins(0, 8, 0, 8);
            llKeys.addView(tvKey, layoutParams);
            TextView tvDivider = new TextView(context);
            tvDivider.setBackgroundColor(Color.BLACK);
            LinearLayout.LayoutParams layoutparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutparams.width = getResources().getDisplayMetrics().widthPixels;
            layoutparams.height = 2;
            llKeys.addView(tvDivider, layoutparams);
            switch (keys[i]) {
                case "Volume up":
                    integerViewMap.put(KeyEvent.KEYCODE_VOLUME_UP, tvKey);
                    continue;
                case "Volume down":
                    integerViewMap.put(KeyEvent.KEYCODE_VOLUME_DOWN, tvKey);
                    continue;
                case "Home":
                    integerViewMap.put(KeyEvent.KEYCODE_HOME, tvKey);
                    continue;
                case "Menu":
                    integerViewMap.put(KeyEvent.KEYCODE_MENU, tvKey);
                    continue;
                case "Back":
                    integerViewMap.put(KeyEvent.KEYCODE_BACK, tvKey);
                    continue;
            }
        }
    }

    @Override
    public void onKeyDown(int keyCode) {
        setBgColor(keyCode);
    }

    private void setBgColor(int keyCode) {
        TextView tvKey = (TextView) integerViewMap.get(keyCode);
        if (tvKey != null) {
            int tag = (int) tvKey.getTag();
            switch (tag) {
                case 0:
                case 2:
                    tvKey.setBackgroundColor(Color.GREEN);
                    tvKey.setTag(1);
                    break;
                case 1:
                    tvKey.setBackgroundColor(Color.RED);
                    tvKey.setTag(2);
                    break;
            }
        }
        if (Config.AUTO_TEST.equals(getMode())) {
            if ((int) integerViewMap.get(KeyEvent.KEYCODE_VOLUME_UP).getTag() != 0 &&
                    (int) integerViewMap.get(KeyEvent.KEYCODE_VOLUME_DOWN).getTag() != 0 &&
                    (int) integerViewMap.get(KeyEvent.KEYCODE_HOME).getTag() != 0 &&
                    (int) integerViewMap.get(KeyEvent.KEYCODE_MENU).getTag() != 0 &&
                    (int) integerViewMap.get(KeyEvent.KEYCODE_BACK).getTag() != 0) {
                Log.i("zhangziran", "setBgColor:handler " + getTotalName());
                handler.obtainMessage(2).sendToTarget();
                updateDataBase(Config.SUCCESS);
            }
        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        if (integerViewMap != null) {
            integerViewMap.clear();
            integerViewMap = null;
        }
    }
}
