package com.magcomm.settingtools.sensorsettings;

import android.animation.Animator;
import android.animation.PointFEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Created by hucheng on 2018/1/27.
 */

public class GravityAnimView extends View{
    private float mWidthX;
    private float mHeightY;
    private float mSpeed;
    private Paint mPaint;
    private int mWidth;
    private int mHeight;
    private final int MAX_HEIGHT=13;
    private PointF mCurrentPoint;
    private boolean mRun;
    private RadialGradient mRadialGradient;
    private Paint mSharedPaint;
	private float mX;
	private float mY;
	private float mPx;
	private float mPy;
	private final static float FOOT_SPEED=15f;
    public GravityAnimView(Context context) {
        super(context);
        init();
    }

    public GravityAnimView(Context context,  AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GravityAnimView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    public void setValue(float x,float y){
        //mWidthX=x;
       //mHeightY=y;
		mPx=x;
		mPy=y;
    }
    private void init(){
        mPaint=new Paint();
        mPaint.setStrokeWidth(2);
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.parseColor("#f43838"));
        mSharedPaint=new Paint();
        mSharedPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth=MeasureSpec.getSize(widthMeasureSpec);
        mHeight=MeasureSpec.getSize(heightMeasureSpec);
        mRadialGradient = new RadialGradient(mWidth/2, mHeight/2, mWidth/2, new int[] {Color.parseColor("#0c5b1b"), Color.parseColor("#c7eac6")}, null, Shader.TileMode.CLAMP);
        mSharedPaint.setShader(mRadialGradient);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mSharedPaint==null){
            return;
        }
		float x=Math.abs(mPx-mX)/FOOT_SPEED;
		float y=Math.abs(mPy-mY)/FOOT_SPEED;
		if(mPx>mX){
			mX+=x;
		}else if(mPx<mX){
			mX-=x;
		}
		if(mPy>mY){
			mY+=y;
		}else if(mPy<mY){
			mY-=y;
		}
        canvas.drawCircle(mWidth/2,mHeight/2,mWidth/2-15,mSharedPaint);
        /*if ((mCurrentPoint.x*mCurrentPoint.x+mCurrentPoint.y*mCurrentPoint.y)<(MAX_HEIGHT-1)*(MAX_HEIGHT-1)){
            canvas.drawCircle(mWidth/2-mCurrentPoint.x*mWidth/2/MAX_HEIGHT,mHeight/2+mCurrentPoint.y*mHeight/2/MAX_HEIGHT,15f,mPaint);
        }*/
		if((mX*mX+mY*mY)<(MAX_HEIGHT-1)*(MAX_HEIGHT-1)){
			canvas.drawCircle(mWidth/2-mX*mWidth/2/MAX_HEIGHT,mHeight/2+mY*mHeight/2/MAX_HEIGHT,15f,mPaint);
		}
		invalidate();
    }
    private void startAnimaView(float startX,float startY){
        PointF startPoint = new PointF(startX, startY);
        final float x=mWidthX;
        final float y=mHeightY;
        PointF endPoint = new PointF(x,y);
        ValueAnimator anim = ValueAnimator.ofObject(new PointFEvaluator(),startPoint,endPoint);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
				if(!mRun){
					anim.cancel();
				}
                mCurrentPoint = (PointF) animation.getAnimatedValue();
                invalidate();

            }
        });
        anim.setInterpolator(new DecelerateInterpolator());
        anim.setDuration(300);
        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
				if(mRun){
                	startAnimaView(x,y);
				}
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }
	public void stop(){
		mRun=false;
	}
	public void start(){
		mRun=true;
        startAnimaView(mWidth/2,mHeight/2);
	}
}
