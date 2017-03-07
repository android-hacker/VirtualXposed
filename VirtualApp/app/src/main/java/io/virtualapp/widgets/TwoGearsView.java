package io.virtualapp.widgets;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;


public class TwoGearsView extends BaseView {
    ValueAnimator valueAnimator = null;
    float mAnimatedValue = 0f;
    float hypotenuse = 0f;
    float smallRingCenterX = 0f;
    float smallRingCenterY = 0f;
    float bigRingCenterX = 0f;
    float bigRingCenterY = 0f;
    private float mWidth = 0f;
    private Paint mPaint, mPaintAxle;
    private Paint mPaintRing;
    private float mPadding = 0f;
    private float mWheelLength;
    private int mWheelSmallSpace = 10;
    private int mWheelBigSpace = 8;

    public TwoGearsView(Context context) {
        super(context);
    }

    public TwoGearsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TwoGearsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getMeasuredWidth() > getHeight())
            mWidth = getMeasuredHeight();
        else
            mWidth = getMeasuredWidth();
    }


    private void drawSmallRing(Canvas canvas) {

        hypotenuse = (float) (mWidth * Math.sqrt(2));
        smallRingCenterX = (float) ((hypotenuse / 6.f) * Math.cos(45 * Math.PI / 180f));
        smallRingCenterY = (float) ((hypotenuse / 6.f) * Math.sin(45 * Math.PI / 180f));
        mPaintRing.setStrokeWidth(dip2px(1.0f));
        canvas.drawCircle(mPadding + smallRingCenterX, smallRingCenterY + mPadding, smallRingCenterX, mPaintRing);
        mPaintRing.setStrokeWidth(dip2px(1.5f));
        canvas.drawCircle(mPadding + smallRingCenterX, smallRingCenterY + mPadding, smallRingCenterX / 2, mPaintRing);
    }


    private void drawSmallGear(Canvas canvas) {

        mPaint.setStrokeWidth(dip2px(1));

        for (int i = 0; i < 360; i = i + mWheelSmallSpace) {
            int angle = (int) (mAnimatedValue * mWheelSmallSpace + i);
            float x3 = (float) ((smallRingCenterX) * Math.cos(angle * Math.PI / 180f));
            float y3 = (float) ((smallRingCenterY) * Math.sin(angle * Math.PI / 180f));
            float x4 = (float) ((smallRingCenterX + mWheelLength) * Math.cos(angle * Math.PI / 180f));
            float y4 = (float) ((smallRingCenterY + mWheelLength) * Math.sin(angle * Math.PI / 180f));

            canvas.drawLine(mPadding + smallRingCenterX - x4,
                    smallRingCenterY + mPadding - y4,
                    smallRingCenterX + mPadding - x3,
                    smallRingCenterY + mPadding - y3,
                    mPaint);

        }

    }

    private void drawBigGear(Canvas canvas) {
        bigRingCenterX = (float) ((hypotenuse / 2.f) * Math.cos(45 * Math.PI / 180f));
        bigRingCenterY = (float) ((hypotenuse / 2.f) * Math.sin(45 * Math.PI / 180f));
        float strokeWidth = dip2px(1.5f) / 4;
        mPaint.setStrokeWidth(dip2px(1.5f));
        for (int i = 0; i < 360; i = i + mWheelBigSpace) {
            int angle = (int) (360 - (mAnimatedValue * mWheelBigSpace + i));
            float x3 = (float) ((bigRingCenterX - smallRingCenterX) * Math.cos(angle * Math.PI / 180f));
            float y3 = (float) ((bigRingCenterY - smallRingCenterY) * Math.sin(angle * Math.PI / 180f));
            float x4 = (float) ((bigRingCenterX - smallRingCenterX + mWheelLength) * Math.cos(angle * Math.PI / 180f));
            float y4 = (float) ((bigRingCenterY - smallRingCenterY + mWheelLength) * Math.sin(angle * Math.PI / 180f));
            canvas.drawLine(bigRingCenterX + mPadding - x4 + mWheelLength * 2 + strokeWidth,
                    bigRingCenterY + mPadding - y4 + mWheelLength * 2 + strokeWidth,
                    bigRingCenterX + mPadding - x3 + mWheelLength * 2 + strokeWidth,
                    bigRingCenterY + mPadding - y3 + mWheelLength * 2 + strokeWidth,
                    mPaint);

        }

    }

    private void drawBigRing(Canvas canvas) {
        float strokeWidth = dip2px(1.5f) / 4;
        mPaintRing.setStrokeWidth(dip2px(1.5f));
        canvas.drawCircle(bigRingCenterX + mPadding + mWheelLength * 2 + strokeWidth,
                bigRingCenterY + mPadding + mWheelLength * 2 + strokeWidth,
                bigRingCenterX - smallRingCenterX - strokeWidth, mPaintRing);
        mPaintRing.setStrokeWidth(dip2px(1.5f));
        canvas.drawCircle(bigRingCenterX + mPadding + mWheelLength * 2 + strokeWidth,
                bigRingCenterY + mPadding + mWheelLength * 2 + strokeWidth,
                (bigRingCenterX - smallRingCenterX) / 2 - strokeWidth, mPaintRing);

    }


    private void drawAxle(Canvas canvas) {


        for (int i = 0; i < 3; i++) {
            float x3 = (float) ((smallRingCenterX) * Math.cos(i * (360 / 3) * Math.PI / 180f));
            float y3 = (float) ((smallRingCenterY) * Math.sin(i * (360 / 3) * Math.PI / 180f));
            canvas.drawLine(mPadding + smallRingCenterX,
                    mPadding + smallRingCenterY,
                    mPadding + smallRingCenterX - x3,
                    mPadding + smallRingCenterY - y3, mPaintAxle);

        }

        for (int i = 0; i < 3; i++) {
            float x3 = (float) ((bigRingCenterX - smallRingCenterX) * Math.cos(i * (360 / 3) * Math.PI / 180f));
            float y3 = (float) ((bigRingCenterY - smallRingCenterY) * Math.sin(i * (360 / 3) * Math.PI / 180f));
            canvas.drawLine(bigRingCenterX + mPadding + mWheelLength * 2,
                    bigRingCenterY + mPadding + mWheelLength * 2,
                    bigRingCenterX + mPadding + mWheelLength * 2 - x3,
                    bigRingCenterY + mPadding + mWheelLength * 2 - y3,
                    mPaintAxle);

        }


    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPadding = dip2px(5);
        canvas.save();
        canvas.rotate(180, mWidth / 2, mWidth / 2);
        drawSmallRing(canvas);
        drawSmallGear(canvas);
        drawBigGear(canvas);
        drawBigRing(canvas);
        drawAxle(canvas);
        canvas.restore();
    }

    private void initPaint() {
        mPaintRing = new Paint();
        mPaintRing.setAntiAlias(true);
        mPaintRing.setStyle(Paint.Style.STROKE);
        mPaintRing.setColor(Color.WHITE);
        mPaintRing.setStrokeWidth(dip2px(1.5f));


        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(dip2px(1));


        mPaintAxle = new Paint();
        mPaintAxle.setAntiAlias(true);
        mPaintAxle.setStyle(Paint.Style.FILL);
        mPaintAxle.setColor(Color.WHITE);
        mPaintAxle.setStrokeWidth(dip2px(1.5f));
        mWheelLength = dip2px(2f);


    }

    public void setViewColor(int color) {
        mPaint.setColor(color);
        mPaintAxle.setColor(color);
        mPaintRing.setColor(color);
        postInvalidate();
    }


    @Override
    protected void InitPaint() {
        initPaint();
    }

    @Override
    protected void OnAnimationUpdate(ValueAnimator valueAnimator) {
        mAnimatedValue = (float) valueAnimator.getAnimatedValue();
        postInvalidate();
    }

    @Override
    protected void OnAnimationRepeat(Animator animation) {

    }

    @Override
    protected int OnStopAnim() {
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

    private int dip2px(float dpValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}