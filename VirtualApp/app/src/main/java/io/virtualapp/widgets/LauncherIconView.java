package io.virtualapp.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.DecelerateInterpolator;

import io.virtualapp.R;

import static android.graphics.Canvas.ALL_SAVE_FLAG;

public class LauncherIconView extends AppCompatImageView implements ShimmerViewBase {
    private static final int SMOOTH_ANIM_THRESHOLD = 5;

    private static final String TAG = "LauncherIconView";

    private ShimmerViewHelper mShimmerViewHelper;
    private Shimmer mShimmer;

    private float mProgress;
    private int mHeight;
    private int mWidth;
    private int mStrokeWidth;
    private float mRadius;
    private float mInterDelta;
    private int mMaskColor;

    private float mMaxMaskRadius;
    private float mMaskAnimDelta;
    private boolean mIsSquare;
    private boolean mMaskAnimRunning;

    private long mMediumAnimTime;

    private Paint mShimmerPaint;
    private Paint mPaint;
    private RectF mProgressOval;
    private ValueAnimator mInterAnim;
    private ValueAnimator mProgressAnimator;

    public LauncherIconView(Context context) {
        super(context);
        init(context, null);
    }

    public LauncherIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public LauncherIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mMediumAnimTime = getContext().getResources().getInteger(android.R.integer.config_mediumAnimTime);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressImageView);
        try {
            this.mProgress = a.getInteger(R.styleable.ProgressImageView_pi_progress, 0);
            this.mStrokeWidth = a.getDimensionPixelOffset(R.styleable.ProgressImageView_pi_stroke, 8);
            this.mRadius = a.getDimensionPixelOffset(R.styleable.ProgressImageView_pi_radius, 0);
            this.mIsSquare = a.getBoolean(R.styleable.ProgressImageView_pi_force_square, false);
            this.mMaskColor = a.getColor(R.styleable.ProgressImageView_pi_mask_color, Color.argb(180, 0, 0, 0));

            this.mPaint = new Paint();
            mPaint.setColor(mMaskColor);
            mPaint.setAntiAlias(true);

            this.mShimmerPaint = new Paint();
            mShimmerPaint.setColor(Color.WHITE);
        } finally {
            a.recycle();
        }
        mShimmerViewHelper = new ShimmerViewHelper(this, mShimmerPaint, attrs);
    }

    private void initParams() {
        if (mWidth == 0)
            mWidth = getWidth();

        if (mHeight == 0)
            mHeight = getHeight();

        if (mWidth != 0 && mHeight != 0) {
            if (mRadius == 0)
                mRadius = Math.min(mWidth, mHeight) / 4f;

            if (mMaxMaskRadius == 0)
                mMaxMaskRadius = (float) (0.5f * Math.sqrt(mWidth * mWidth + mHeight * mHeight));

            if (mProgressOval == null)
                mProgressOval = new RectF(
                        mWidth / 2f - mRadius + mStrokeWidth,
                        mHeight / 2f - mRadius + mStrokeWidth,
                        mWidth / 2f + mRadius - mStrokeWidth,
                        mHeight / 2f + mRadius - mStrokeWidth);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.onDraw();
        }
        super.onDraw(canvas);
        int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, ALL_SAVE_FLAG);

        initParams();

        if (mProgress < 100) {
            drawMask(canvas);

            if (mProgress == 0)
                updateInterAnim(canvas);
            else
                drawProgress(canvas);
        }

        if (mMaskAnimRunning)
            updateMaskAnim(canvas);

        canvas.restoreToCount(sc);
    }

    private void drawMask(Canvas canvas) {
        canvas.drawRect(0, 0, mWidth, mHeight, mPaint);
    }

    private void drawProgress(Canvas canvas) {
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius, mPaint);
        mPaint.setXfermode(null);

        //start angle : -90 ~ 270;sweep Angle : 360 ~ 0;
        canvas.drawArc(mProgressOval, -90 + mProgress * 3.6f, 360 - mProgress * 3.6f, true, mPaint);
    }

    private void updateInterAnim(Canvas canvas) {
//        if (!mInterAnimRunning) mInterDelta = 0.f;

        //outer circle
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawCircle(mWidth / 2.f, mHeight / 2.f, mRadius, mPaint);
        mPaint.setXfermode(null);

        //inner circle
        canvas.drawCircle(mWidth / 2.f, mHeight / 2.f, mRadius - mInterDelta, mPaint);
    }

    private void updateMaskAnim(Canvas canvas) {
        canvas.drawRect(0, 0, mWidth, mHeight, mPaint);

        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        canvas.drawCircle(mWidth / 2f, mHeight / 2f, mRadius + mMaskAnimDelta, mPaint);//mRatio : 0 ~ mRatio * 1.5
        mPaint.setXfermode(null);
    }

    private void startInterAnim(final int progress) {
        if (mInterAnim != null)
            mInterAnim.cancel();

        mInterAnim = ValueAnimator.ofFloat(0.f, mStrokeWidth);
        mInterAnim.setInterpolator(new DecelerateInterpolator());
        mInterAnim.setDuration(getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
        mInterAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mInterDelta = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mInterAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
//                mInterAnimRunning = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
//                mInterAnimRunning = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
//                mInterAnimRunning = false;

                if (progress > 0)
                    startProgressAnim(0, progress);
            }
        });
        mInterAnim.start();
    }

    private void startProgressAnim(float from, float to) {
        if (mProgressAnimator != null)
            mProgressAnimator.cancel();

        final boolean isReverse = from > to;

        mProgressAnimator = ValueAnimator.ofFloat(from, to);
        mProgressAnimator.setInterpolator(new DecelerateInterpolator());
        mProgressAnimator.setDuration(mMediumAnimTime);
        mProgressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mProgress = (float) animation.getAnimatedValue();

                if (0 < mProgress && mProgress < 100)
                    invalidate();
                else if (mProgress == 100 && !isReverse)
                    startMaskAnim();
            }
        });
        mProgressAnimator.start();
    }

    private void startMaskAnim() {
        if (mProgressAnimator != null)
            mProgressAnimator.cancel();

        ValueAnimator animator = ValueAnimator.ofFloat(0.f, mMaxMaskRadius);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(mMediumAnimTime);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mMaskAnimRunning = true;
                mMaskAnimDelta = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mMaskAnimRunning = true;
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                mMaskAnimRunning = false;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mMaskAnimRunning = false;
            }
        });
        animator.start();
    }

    /**
     * get the stroke width.
     *
     * @return the stroke width in pixel.
     */
    public int getStrokeWidth() {
        return mStrokeWidth;
    }

    /**
     * set the stroke width.default is 8dp.
     *
     * @param strokeWidth stroke width in pixel
     */
    public void setStrokeWidth(int strokeWidth) {
        this.mStrokeWidth = strokeWidth;
        this.mProgressOval = null;
        invalidate();
    }

    /**
     * get the radius of inner progress circle.
     *
     * @return the inner circle radius in pixel.
     */
    public float getRadius() {
        return mRadius;
    }

    /**
     * set the radius of the inner progress circle.
     *
     * @param radius radius in pixel
     */
    public void setRadius(float radius) {
        this.mRadius = radius;
        this.mProgressOval = null;
        invalidate();
    }

    /**
     * get the color for mask .
     *
     * @return the mask color
     */
    public int getMaskColor() {
        return mMaskColor;
    }

    /**
     * set the color for mask. Argb will looks better. Default is Color.argb(180,0,0,0)
     *
     * @param maskColor the color value.
     */
    public void setMaskColor(int maskColor) {
        mMaskColor = maskColor;
        mPaint.setColor(mMaskColor);
        invalidate();
    }

    /**
     * get current progress.
     *
     * @return current progress value.
     */
    public int getProgress() {
        return (int) mProgress;
    }

    /**
     * @param progress the progress ,range [0,100]
     */
    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    /**
     * @param progress the progress in [0,100]
     * @param animate  true to enable smooth animation when progress changed more than 5.
     */
    public void setProgress(int progress, boolean animate) {
        progress = Math.min(Math.max(progress, 0), 100);

        Log.d(TAG, "setProgress: p:" + progress + ",mp:" + mProgress);

        if (Math.abs(progress - mProgress) > SMOOTH_ANIM_THRESHOLD && animate) {
            if (mProgress == 0) {
                startInterAnim(progress);
            } else {
                startProgressAnim(mProgress, progress);
            }
        } else if (progress == 100 && animate) {
            mProgress = 100;
            startMaskAnim();
        } else {
            mProgress = progress;

            if (mProgress == 0.f)
                mInterDelta = 0.f;

            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mIsSquare) {
            int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
            int size = measuredWidth == 0 ? MeasureSpec.getSize(heightMeasureSpec) : measuredWidth;
            setMeasuredDimension(size, size);
        }
    }


    @Override
    public float getGradientX() {
        return mShimmerViewHelper.getGradientX();
    }

    @Override
    public void setGradientX(float gradientX) {
        mShimmerViewHelper.setGradientX(gradientX);
    }

    @Override
    public boolean isShimmering() {
        return mShimmerViewHelper.isShimmering();
    }

    @Override
    public void setShimmering(boolean isShimmering) {
        mShimmerViewHelper.setShimmering(isShimmering);
    }

    @Override
    public boolean isSetUp() {
        return mShimmerViewHelper.isSetUp();
    }

    @Override
    public void setAnimationSetupCallback(ShimmerViewHelper.AnimationSetupCallback callback) {
        mShimmerViewHelper.setAnimationSetupCallback(callback);
    }

    @Override
    public int getPrimaryColor() {
        return mShimmerViewHelper.getPrimaryColor();
    }

    @Override
    public void setPrimaryColor(int primaryColor) {
        mShimmerViewHelper.setPrimaryColor(primaryColor);
    }

    @Override
    public int getReflectionColor() {
        return mShimmerViewHelper.getReflectionColor();
    }

    @Override
    public void setReflectionColor(int reflectionColor) {
        mShimmerViewHelper.setReflectionColor(reflectionColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mShimmerViewHelper != null) {
            mShimmerViewHelper.onSizeChanged();
        }
    }

    public void stopShimmer() {
        if (mShimmer != null && mShimmer.isAnimating()) {
            mShimmer.cancel();
            mShimmer = null;
        }
    }

    public void startShimmer() {
        stopShimmer();
        mShimmer = new Shimmer();
        mShimmer.setRepeatCount(1)
                .setStartDelay(800L)
                .setDirection(Shimmer.ANIMATION_DIRECTION_LTR)
                .start(this);
    }
}