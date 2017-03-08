package io.virtualapp.widgets;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;


public class EatBeansView extends BaseView {

    int eatSpeed = 5;
    private Paint mPaint, mPaintEye;
    private float mWidth = 0f;
    private float mHigh = 0f;
    private float mPadding = 5f;
    private float eatErWidth = 60f;
    private float eatErPositionX = 0f;
    private float beansWidth = 10f;


    private float mAngle = 34;
    private float eatErStartAngle = mAngle;
    private float eatErEndAngle = 360 - 2 * eatErStartAngle;

    public EatBeansView(Context context) {
        super(context);
    }

    public EatBeansView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EatBeansView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        mWidth = getMeasuredWidth();
        mHigh = getMeasuredHeight();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float eatRightX = mPadding + eatErWidth + eatErPositionX;
        RectF rectF = new RectF(mPadding + eatErPositionX, mHigh / 2 - eatErWidth / 2, eatRightX, mHigh / 2 + eatErWidth / 2);
        canvas.drawArc(rectF, eatErStartAngle, eatErEndAngle
                , true, mPaint);
        canvas.drawCircle(mPadding + eatErPositionX + eatErWidth / 2,
                mHigh / 2 - eatErWidth / 4,
                beansWidth / 2, mPaintEye);

        int beansCount = (int) ((mWidth - mPadding * 2 - eatErWidth) / beansWidth / 2);
        for (int i = 0; i < beansCount; i++) {

            float x = beansCount * i + beansWidth / 2 + mPadding + eatErWidth;
            if (x > eatRightX) {
                canvas.drawCircle(x,
                        mHigh / 2, beansWidth / 2, mPaint);
            }
        }


    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.WHITE);

        mPaintEye = new Paint();
        mPaintEye.setAntiAlias(true);
        mPaintEye.setStyle(Paint.Style.FILL);
        mPaintEye.setColor(Color.BLACK);

    }


    public void setViewColor(int color) {
        mPaint.setColor(color);
        postInvalidate();
    }

    public void setEyeColor(int color) {
        mPaintEye.setColor(color);
        postInvalidate();
    }


    @Override
    protected void InitPaint() {
        initPaint();
    }

    @Override
    protected void OnAnimationUpdate(ValueAnimator valueAnimator) {
        float mAnimatedValue = (float) valueAnimator.getAnimatedValue();
        eatErPositionX = (mWidth - 2 * mPadding - eatErWidth) * mAnimatedValue;
        eatErStartAngle = mAngle * (1 - (mAnimatedValue * eatSpeed - (int) (mAnimatedValue * eatSpeed)));
        eatErEndAngle = 360 - eatErStartAngle * 2;
        invalidate();
    }

    @Override
    protected void OnAnimationRepeat(Animator animation) {

    }

    @Override
    protected int OnStopAnim() {
        eatErPositionX = 0;
        postInvalidate();
        return 1;
    }

    @Override
    protected int SetAnimRepeatMode() {
        return ValueAnimator.RESTART;
    }

    @Override
    protected void AnimIsRunning() {

    }

    @Override
    protected int SetAnimRepeatCount() {
        return ValueAnimator.INFINITE;
    }
}