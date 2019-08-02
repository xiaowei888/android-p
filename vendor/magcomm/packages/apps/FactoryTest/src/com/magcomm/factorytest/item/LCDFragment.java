package com.magcomm.factorytest.item;

import android.graphics.Color;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/21.
 */

public class LCDFragment extends BaseFragment {
    private int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE, Color.BLACK, Color.WHITE};
    private int currentPosition = 0;

    @Override
    protected int getCurrentView() {
        return R.layout.lcd_test;
    }

    @Override
    protected void onFragmentCreat() {
        view.setBackgroundColor(colors[currentPosition]);
        handler.postDelayed(runnable, 1500);
    }


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            currentPosition++;
            if (currentPosition >= colors.length) {
                currentPosition = 0;
            }
            if (currentPosition == 0) {
                if (Config.AUTO_TEST.equals(getMode())) {
                    handler.removeCallbacks(runnable);
                    updateDataBase(Config.SUCCESS);
                    handler.obtainMessage(2).sendToTarget();
                    return;
                }
            }
            view.setBackgroundColor(colors[currentPosition]);
            handler.postDelayed(this, 1500);
        }
    };

    @Override
    public void onUp(int x, int y) {
        onNext();
    }

    public void onNext() {
        handler.removeCallbacks(runnable);
        currentPosition++;
        if (currentPosition >= colors.length) {
            currentPosition = 0;
        }
        if (currentPosition == 0) {
            if (Config.AUTO_TEST.equals(getMode())) {
                handler.removeCallbacks(runnable);
                updateDataBase(Config.SUCCESS);
                handler.obtainMessage(2).sendToTarget();
                return;
            }
        }
        view.setBackgroundColor(colors[currentPosition]);
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        handler.removeCallbacks(runnable);
        this.runnable = null;
    }
}
