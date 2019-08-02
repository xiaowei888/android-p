package com.magcomm.factorytest.batterytest.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.batterytest.activity.BatteryBaseActivity;

/**
 * Created by zhangziran on 2017/11/30.
 */

public class VibrationTestFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private View view;
    private ImageView ivShake;
    private Vibrator vibrator;
    private CountDownTimer timer;
    private Handler handler;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof BatteryBaseActivity) {
            handler = ((BatteryBaseActivity) activity).getHandler();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.vibrate_test, null);
        ivShake = (ImageView)view.findViewById(R.id.iv_shake);
        initVibrator();
        return view;
    }
    int num =0;
    private void initVibrator() {
        if(vibrator==null) {
            vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
        }
        long shake[] = {0l,1000l,1000l,1000l,1000l,1000l,1000l,1000l,1000l,1000l};
        vibrator.vibrate(shake,-1);
        timer = new CountDownTimer(14000,2000) {
            @Override
            public void onTick(long millisUntilFinished) {
                num++;
                if(num ==1 || num ==2||num ==3||num ==4||num ==5){
                    ivShake.clearAnimation();
                    Animation shakeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                    ivShake.setAnimation(shakeAnim);
                }
                if(num == 6) {
                    handler.obtainMessage(0).sendToTarget();
                    num =0;
                    timer.onFinish();
                }
            }
            @Override
            public void onFinish() {
                vibrator.cancel();
                timer.cancel();
                timer=null;
            }
        };
        timer.start();
    }

    @Override
    public void reset(boolean fail) {
        if(timer!=null) {
            timer.cancel();
            timer = null;
        }
        vibrator.cancel();
    }


}
