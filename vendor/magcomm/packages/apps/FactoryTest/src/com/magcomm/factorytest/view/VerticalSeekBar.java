package com.magcomm.factorytest.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.magcomm.factorytest.R;

public class VerticalSeekBar extends View {
    private static final String TAG = "zhangziran";
    private float height, width;
    private Paint bgPaint, bgRedPaint, circlePaint;
    private float progress = 0.5f;
    private int left, top;
    private OnProgressChangeListener progressChangeListener;

    public VerticalSeekBar(Context context) {
        super(context);
        init();
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public VerticalSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        bgPaint = new Paint();
        bgPaint.setAntiAlias(true);
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.parseColor("#bbbbbb"));

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(getResources().getColor(R.color.colorAccent, null));

        bgRedPaint = new Paint();
        bgRedPaint.setAntiAlias(true);
        bgRedPaint.setStyle(Paint.Style.FILL);
        bgRedPaint.setColor(getResources().getColor(R.color.colorAccent, null));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        width = getSize(widthMeasureSpec, 0);
        height = getSize(heightMeasureSpec, 1);
        setMeasuredDimension((int) width, (int) height);
    }

    private float getSize(int measureSpec, int direction) {
        int result = 0;
        int mode = MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (direction == 0 && size < 50 || direction == 1 && size < 120) {
            mode = MeasureSpec.AT_MOST;
        }
        switch (mode) {
            case MeasureSpec.EXACTLY:
                result = size;
                break;
            case MeasureSpec.AT_MOST:
                if (direction == 0) {
                    result = 50;
                } else if (direction == 1) {
                    result = 360;
                }
                break;
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(width / 2 - 2f, 30, width / 2 + 2f, height - 30, bgPaint);
        canvas.drawRect(width / 2 - 2f, 30 + progress * (height - 60), width / 2 + 2f, height - 30, bgRedPaint);
        canvas.drawCircle(width / 2, 30 + progress * (height - 60), 10, circlePaint);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        this.left = getLeft() + getPaddingLeft();
        this.top = getTop() + getPaddingTop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        if (isAvailable(x, y)) {
            progress = (y - top - 30) / (height - 60);
            if (progressChangeListener != null) {
                if(progress > 0.95) {
                    progress = 1;
                }else if(progress < 0.05) {
                    progress = 0;
                }
                progressChangeListener.onProgressChangeListener(1 - progress);
            }
            invalidate();
        }
        return true;
    }

    private boolean isAvailable(float x, float y) {
        boolean available = x > left && x < (left + width) && y > (top + 30) && y < (top + height - 30);
        return available;
    }

    public void setProgress(float progress) {
        this.progress = 1-progress;
        invalidate();
    }

    public interface OnProgressChangeListener {
        void onProgressChangeListener(float progress);
    }

    public void setOnProgressChangeListener(OnProgressChangeListener progressChangeListener) {
        this.progressChangeListener = progressChangeListener;
    }


    public void removeProgressChangeListener() {
        this.progressChangeListener=null;
    }

}
