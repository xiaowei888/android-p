package com.magcomm.factorytest.util;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * Created by zhangziran on 2017/12/20.
 */

public class BackGroundUtil {
    private static final String TAG = "zhangziran";
    
    private static final GradientDrawable normalShapePressed = new GradientDrawable();
    private static final GradientDrawable normalShapeNoPressed = new GradientDrawable();
    private static final GradientDrawable successShapePressed = new GradientDrawable();
    private static final GradientDrawable successShapeNoPressed = new GradientDrawable();
    private static final GradientDrawable failShapePressed = new GradientDrawable();
    private static final GradientDrawable failShapeNoPressed = new GradientDrawable();

    private static final StateListDrawable normalSelector = new StateListDrawable();
    private static final StateListDrawable successSelector = new StateListDrawable();
    private static final StateListDrawable failSelector = new StateListDrawable();

    private static final int roundRadius = 20;
    private static final String normalColorPressed = "#eeeeee";
    private static final String normalColorNoPressed = "#9e9e9e";
    private static final String successColorPressed = "#00ff11";
    private static final String successColorNoPressed = "#11ff44";
    private static final String failColorPressed = "#FF0000";
    private static final String failColorNoPressed = "#FF4444";

    private BackGroundUtil() {
        init();
    }

    private static final class UtilHandler {
        private static final BackGroundUtil backGroundUtil = new BackGroundUtil();
    }

    public static BackGroundUtil getInstance() {
        return UtilHandler.backGroundUtil;
    }

    private void init() {
        normalShapePressed.setCornerRadius(roundRadius);
        normalShapePressed.setColor(Color.parseColor(normalColorPressed));

        normalShapeNoPressed.setCornerRadius(roundRadius);
        normalShapeNoPressed.setColor(Color.parseColor(normalColorNoPressed));

        successShapePressed.setCornerRadius(roundRadius);
        successShapePressed.setColor(Color.parseColor(successColorPressed));

        successShapeNoPressed.setCornerRadius(roundRadius);
        successShapeNoPressed.setColor(Color.parseColor(successColorNoPressed));

        failShapePressed.setCornerRadius(roundRadius);
        failShapePressed.setColor(Color.parseColor(failColorPressed));

        failShapeNoPressed.setCornerRadius(roundRadius);
        failShapeNoPressed.setColor(Color.parseColor(failColorNoPressed));

        normalSelector.addState(new int[]{android.R.attr.state_pressed}, normalShapePressed);
        normalSelector.addState(new int[]{-android.R.attr.state_pressed}, normalShapeNoPressed);

        successSelector.addState(new int[]{android.R.attr.state_pressed}, successShapePressed);
        successSelector.addState(new int[]{-android.R.attr.state_pressed}, successShapeNoPressed);

        failSelector.addState(new int[]{android.R.attr.state_pressed}, failShapePressed);
        failSelector.addState(new int[]{-android.R.attr.state_pressed}, failShapeNoPressed);
    }

    public static void setBG(Button btn, int model) {
        switch (model) {
            case 0:
                btn.setBackground(failSelector);
                break;
            case 1:
                btn.setBackground(successSelector);
                break;
            case 2:
            default:
                btn.setBackground(normalSelector);
                break;
        }
    }

}
