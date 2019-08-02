package com.magcomm.factorytest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import com.magcomm.factorytest.R;


/**
 * Created by zhangziran on 2017/12/1.
 */

public class MikeView extends View {
    static final float PIVOT_RADIUS = 3.5f;
    static final float PIVOT_Y_OFFSET = 10f;
    static final float SHADOW_OFFSET = 2.0f;
    static final float DROPOFF_STEP = 0.18f;
    static final float SURGE_STEP = 0.35f;
    static final long ANIMATION_INTERVAL = 70;
    int volume = 0;
    Paint mPaint, mShadow;
    float mCurrentAngle;

    public MikeView(Context context) {
        super(context);
        init(context);
    }

    public MikeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    void init(Context context) {
        Drawable background = context.getResources().getDrawable(R.mipmap.vumeter, null);
        setBackground(background);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadow.setColor(Color.argb(60, 0, 0, 0));
        mCurrentAngle = 0;
    }

    public void setVolume(int vol) {
        volume = vol;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // final float minAngle = (float)Math.PI/12;
        // final float maxAngle = (float)Math.PI*11/12;
        final float minAngle = (float) Math.PI / 8;
        final float maxAngle = (float) Math.PI * 7 / 8;
        float angle = minAngle;
        if (true) {
            angle += (float) (maxAngle - minAngle) * volume / 4000;
        } else {
            angle = minAngle;
        }

        if (angle > mCurrentAngle)
            mCurrentAngle = angle;
        else
            mCurrentAngle = Math.max(angle, mCurrentAngle - DROPOFF_STEP);

        mCurrentAngle = Math.min(maxAngle, mCurrentAngle);

        float w = getWidth();
        float h = getHeight();
        float pivotX = w / 2;
        float pivotY = h - PIVOT_RADIUS - PIVOT_Y_OFFSET;
        float l = h * 4 / 5;
        float sin = (float) Math.sin(mCurrentAngle);
        float cos = (float) Math.cos(mCurrentAngle);
        float x0 = pivotX - l * cos;
        float y0 = pivotY - l * sin;
        canvas.drawLine(x0 + SHADOW_OFFSET, y0 + SHADOW_OFFSET, pivotX + SHADOW_OFFSET, pivotY + SHADOW_OFFSET, mShadow);
        canvas.drawCircle(pivotX + SHADOW_OFFSET, pivotY + SHADOW_OFFSET, PIVOT_RADIUS, mShadow);
        canvas.drawLine(x0, y0, pivotX, pivotY, mPaint);
        canvas.drawCircle(pivotX, pivotY, PIVOT_RADIUS, mPaint);
    }
}
