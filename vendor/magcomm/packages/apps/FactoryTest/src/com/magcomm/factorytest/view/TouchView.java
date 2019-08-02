package com.magcomm.factorytest.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhangziran on 2017/12/22.
 */

public class TouchView extends View {
    private static final String TAG = "zhangziran";
    private static final int numX = 9;
    private static final int numY = 13;
    private int windowWidth, windowHeight;
    private int dectWidth, dectHeight;
    private List<Rect> rects;
    private Canvas canvas;
    private Path path;
    private Paint bgPaint;
    private Paint linePaint;
    private Paint dashPathPaint;
    private Paint pointPaint;
    private Paint cornerPaint;
    private Bitmap bitmap;
    private float downX, downY, firstMoveX, firstMoveY, lastMoveX, lastMoveY, upX, upY;
    private OnTouchResult onTouchResult;

    public TouchView(Context context) {
        super(context);
        init(context);
    }

    public TouchView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TouchView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setBackgroundColor(Color.WHITE);
        rects = new ArrayList<>();
        windowWidth = context.getResources().getDisplayMetrics().widthPixels;
        windowHeight = context.getResources().getDisplayMetrics().heightPixels;
        dectWidth = windowWidth / numX;
        dectHeight = windowHeight / numY;
        for (int i = 0; i < numX; i++) {
            for (int j = 0; j < numY; j++) {
                if (i == 0 || i == 4 || i == 8) {
                    Rect rect = new Rect(i * dectWidth, j * dectHeight, dectWidth, dectHeight);
                    rects.add(rect);
                } else {
                    if (j == 0 || j == 6 || j == 12) {
                        Rect rect = new Rect(i * dectWidth, j * dectHeight, dectWidth, dectHeight);
                        rects.add(rect);
                    }
                }
            }
        }
        rects.add(new Rect(dectWidth, dectHeight, 3 * dectWidth, 5 * dectHeight));
        rects.add(new Rect(5 * dectWidth, dectHeight, 3 * dectWidth, 5 * dectHeight));
        rects.add(new Rect(dectWidth, 7 * dectHeight, 3 * dectWidth, 5 * dectHeight));
        rects.add(new Rect(5 * dectWidth, 7 * dectHeight, 3 * dectWidth, 5 * dectHeight));
        canvas = new Canvas();
        bitmap = Bitmap.createBitmap(windowWidth, windowHeight, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        Paint bitPaint = new Paint();
        bitPaint.setColor(Color.BLACK);
        bitPaint.setAntiAlias(true);
        bitPaint.setStrokeWidth(1);
        bitPaint.setStyle(Paint.Style.STROKE);
        Iterator<Rect> iterator = rects.iterator();
        while (iterator.hasNext()) {
            Rect rect = iterator.next();
            canvas.drawRect(rect.x, rect.y, rect.x + rect.dx, rect.y + rect.dy, bitPaint);
        }
        invalidate();

        path = new Path();
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        bgPaint.setColor(Color.GREEN);

        pointPaint = new Paint();
        pointPaint.setStyle(Paint.Style.FILL);
        pointPaint.setColor(Color.RED);
        pointPaint.setStrokeWidth(3);

        linePaint = new Paint();
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);
        linePaint.setColor(Color.BLUE);

        dashPathPaint = new Paint();
        dashPathPaint.setStyle(Paint.Style.STROKE);
        dashPathPaint.setAntiAlias(true);
        dashPathPaint.setColor(Color.RED);
        Path p = new Path();
        p.addCircle(0, 0, 2, Path.Direction.CCW);
        PathDashPathEffect pathDashPathEffect = new PathDashPathEffect(p, 20, 0, PathDashPathEffect.Style.ROTATE);
        dashPathPaint.setPathEffect(pathDashPathEffect);

        cornerPaint = new Paint();
        cornerPaint.setStyle(Paint.Style.STROKE);
        cornerPaint.setStrokeWidth(2);
        cornerPaint.setColor(Color.MAGENTA);
        CornerPathEffect cornerPathEffect = new CornerPathEffect(100);
        cornerPaint.setPathEffect(cornerPathEffect);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                lastMoveX = downX = event.getRawX();
                lastMoveY = downY = event.getRawY();
                canvas.drawPoint(downX, downY, pointPaint);
                check(downX, downY);
                invalidate();
                if(onTouchResult!=null) {
                    onTouchResult.onDown(downX, downY);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                firstMoveX = event.getRawX();
                firstMoveY = event.getRawY();
                path.moveTo(lastMoveX, lastMoveY);
                path.lineTo(firstMoveX, firstMoveY);
                canvas.drawPath(path, cornerPaint);
                canvas.drawPath(path, dashPathPaint);
                canvas.drawPath(path, linePaint);
                check(firstMoveX, firstMoveY);
                lastMoveX = firstMoveX;
                lastMoveY = firstMoveY;
                invalidate();
                if(onTouchResult!=null) {
                    onTouchResult.onMove(firstMoveX, firstMoveY);
                }
                break;
            case MotionEvent.ACTION_UP:
                upX = event.getRawX();
                upY = event.getRawY();
                if(onTouchResult!=null) {
                    onTouchResult.onUp(upX, upY);
                }
                if (rects != null && rects.size() == 0) {
                    if(onTouchResult!=null) {
                        onTouchResult.onSuccess();
                    }
                }
                break;
        }
        return true;
    }

    private void check(float x, float y) {
        for (Rect rect : rects) {
            if (rect.isInRect(x, y)) {
                canvas.drawRect(rect.x, rect.y, rect.x + rect.dx, rect.y + rect.dy, bgPaint);
                rects.remove(rect);
                break;
            }
        }
    }


    private class Rect {
        private float x, y, dx, dy;

        public Rect(int x, int y, int dx, int dy) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
        }

        public boolean isInRect(float x, float y) {
            if (x > this.x && x < this.x + dx && y > this.y && y < this.y + dy) {
                return true;
            } else {
                return false;
            }
        }
    }


    public int getRects() {
        return rects.size();
    }

    public interface OnTouchResult {
        void onDown(float x, float y);

        void onMove(float x, float y);

        void onUp(float x, float y);

        void onSuccess();
    }

    public void setOnTouchResult(OnTouchResult onTouchResult) {
        this.onTouchResult = onTouchResult;
    }
}
