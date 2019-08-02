package com.magcomm.factorytest.item;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.SeekBar;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.activity.ItemTestActivity;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/23.
 */

public class BrightnessFragment extends BaseFragment implements SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "zhangziran";
    private int originalMode;
    private int originalBrightness;
    private ContentResolver contentResolver;
    private SeekBar seekBar;
    private TextView tvCurrentBrightness;
    private static final int MAX_BRIGHTNESS = 255;
    private ValueAnimator valueAnimator;

    @Override
    protected int getCurrentView() {
        return R.layout.brightness_test;
    }

    @Override
    protected void onFragmentCreat() {
        seekBar = (SeekBar) view.findViewById(R.id.sb_brightness);
        tvCurrentBrightness = (TextView) view.findViewById(R.id.current_brightness);
        initBrightness();
        initAutoTest();
    }

    private void initAutoTest() {
        if (Config.AUTO_TEST.equals(getMode())) {
            valueAnimator = ValueAnimator.ofInt(originalBrightness, 255, 0, originalBrightness);
            valueAnimator.setDuration(5000);
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int position = (int) animation.getAnimatedValue();
                    seekBar.setProgress(position);
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.SUCCESS);
                }
            });
            valueAnimator.start();
        }
    }

    private void initBrightness() {
        contentResolver = context.getContentResolver();
        originalBrightness = getOriginalBrightness();
        seekBar.setMax(MAX_BRIGHTNESS);
        seekBar.setProgress(originalBrightness);
        tvCurrentBrightness.setText(originalBrightness + "");
        seekBar.setOnSeekBarChangeListener(this);
    }


    private int getOriginalBrightness() {
        ContentResolver contentResolver = getActivity().getContentResolver();
        int defVal = 125;
        return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, defVal);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        tvCurrentBrightness.setText(progress + "");
        setWindowBrightness(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void setWindowBrightness(int brightness) {
        Window window = ((ItemTestActivity) context).getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.screenBrightness = brightness / 255.0f;
        window.setAttributes(lp);
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        if (valueAnimator != null) {
            valueAnimator.removeAllListeners();
            valueAnimator.removeAllUpdateListeners();
            valueAnimator = null;
        }
    }
}
