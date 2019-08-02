package com.magcomm.factorytest.item;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.magcomm.factorytest.R;
import com.magcomm.factorytest.util.Config;
import com.magcomm.factorytest.view.TouchView;

import java.text.DecimalFormat;

/**
 * Created by zhangziran on 2017/12/21.
 */

public class TPFragment extends BaseFragment implements TouchView.OnTouchResult {
    private static final String TAG = "zhangziran";
    private TouchView touchView;
    private float downX, downY, moveX, moveY, upX, upY;
    private PopupWindow popupWindow;
    private View popupView;
    private TextView tvX, tvY, tvDx, tvDy;
    private DecimalFormat decimalFormat;

    @Override
    protected int getCurrentView() {
        return 0;
    }

    @Override
    protected void onFragmentCreat() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        touchView = new TouchView(getContext());
        touchView.setOnTouchResult(this);
        initView();
        return touchView;
    }

    private void initView() {
        LayoutInflater inflater = LayoutInflater.from(context);
        popupView = inflater.inflate(R.layout.popup_layout, null);
        tvX = (TextView) popupView.findViewById(R.id.popup_x);
        tvY = (TextView) popupView.findViewById(R.id.popup_y);
        tvDx = (TextView) popupView.findViewById(R.id.popup_dx);
        tvDy = (TextView) popupView.findViewById(R.id.popup_dy);
        popupWindow = new PopupWindow(popupView, WindowManager.LayoutParams.MATCH_PARENT, 50);
        popupWindow.setTouchable(false);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        decimalFormat = new DecimalFormat("####.##");
    }


    @Override
    public void onDown(float x, float y) {
        if (!popupWindow.isShowing()) {
            popupWindow.showAtLocation(touchView, Gravity.TOP, 0, 0);
        }
        downX = x;
        downY = y;
        tvX.setText(decimalFormat.format(downX));
        tvY.setText(decimalFormat.format(downY));
    }

    @Override
    public void onMove(float x, float y) {
        moveX = x;
        moveY = y;
        tvX.setText(decimalFormat.format(moveX));
        tvY.setText(decimalFormat.format(moveY));
        tvDx.setText(decimalFormat.format(moveX - downX));
        tvDy.setText(decimalFormat.format(moveY - downY));
    }

    @Override
    public void onUp(float x, float y) {
        upX = x;
        upY = y;
        tvX.setText(decimalFormat.format(upX));
        tvY.setText(decimalFormat.format(upY));
        tvDx.setText(decimalFormat.format(upX - downX));
        tvDy.setText(decimalFormat.format(upY - downY));
    }

    @Override
    public void onSuccess() {
        if (Config.AUTO_TEST.equals(getMode())) {
            updateDataBase(Config.SUCCESS);
            handler.obtainMessage(2).sendToTarget();
        }else if(Config.MANUAL_TEST.equals(getMode())) {
            handler.obtainMessage(1).sendToTarget();
        }
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void destroy() {
        popupWindow.dismiss();
        touchView.setOnTouchResult(null);
    }

    @Override
    public void onKeyDown(int keyCode) {
        super.onKeyDown(keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            destroy();
            if (touchView.getRects() != 0) {
                if (Config.AUTO_TEST.equals(getMode())) {
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.FAIL);
                } else if (Config.MANUAL_TEST.equals(getMode())) {
                    Log.i(TAG, "onKeyDown: handler" + getTotalName());
                    handler.obtainMessage(0).sendToTarget();
                }
            } else {
                if (Config.AUTO_TEST.equals(getMode())) {
                    handler.obtainMessage(2).sendToTarget();
                    updateDataBase(Config.SUCCESS);
                } else if (Config.MANUAL_TEST.equals(getMode())) {
                    handler.obtainMessage(1).sendToTarget();
                }
            }
        }
    }
}
