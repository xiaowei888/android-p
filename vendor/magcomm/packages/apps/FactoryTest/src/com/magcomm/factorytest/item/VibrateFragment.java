package com.magcomm.factorytest.item;

import android.content.Context;
import android.os.Vibrator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;

/**
 * Created by zhangziran on 2017/12/23.
 */

public class VibrateFragment extends BaseFragment {
    private ImageView ivShake;
    private Vibrator vibrator;
    private int nums = 0;

    @Override
    protected int getCurrentView() {
        return R.layout.vibrate_test;
    }

    @Override
    protected void onFragmentCreat() {
        ivShake = (ImageView) view.findViewById(R.id.iv_shake);
        initVibrator();
    }


    private void initVibrator() {
        if (vibrator == null) {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
        long[] shake = {1000l, 1000l};
        handler.postDelayed(runnable, 1000);
        vibrator.vibrate(shake, 0);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            ivShake.clearAnimation();
            Animation shakeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
            ivShake.setAnimation(shakeAnim);
            handler.postDelayed(this, 2000);
            if (Config.AUTO_TEST.equals(getMode())) {
                nums++;
                if (nums == 5) {
                    destroy();
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.SUCCESS);
                }
            }
        }
    };

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        vibrator.cancel();
        handler.removeCallbacks(runnable);
        this.runnable = null;
    }
}
