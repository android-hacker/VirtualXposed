package io.virtualapp.widgets;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

public abstract class BaseView extends View {


    public ValueAnimator valueAnimator;

    public BaseView(Context context) {
        this(context, null);
    }

    public BaseView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        InitPaint();
    }

    public void startAnim() {
        stopAnim();
        startViewAnim(0f, 1f, 500);
    }

    public void startAnim(int time) {
        stopAnim();
        startViewAnim(0f, 1f, time);
    }

    public void stopAnim() {
        if (valueAnimator != null) {
            clearAnimation();

            valueAnimator.setRepeatCount(0);
            valueAnimator.cancel();
            valueAnimator.end();
            if (OnStopAnim() == 0) {
                valueAnimator.setRepeatCount(0);
                valueAnimator.cancel();
                valueAnimator.end();
            }

        }
    }

    private ValueAnimator startViewAnim(float startF, final float endF, long time) {
        valueAnimator = ValueAnimator.ofFloat(startF, endF);
        valueAnimator.setDuration(time);
        valueAnimator.setInterpolator(new LinearInterpolator());


        valueAnimator.setRepeatCount(SetAnimRepeatCount());


        if (ValueAnimator.RESTART == SetAnimRepeatMode()) {
            valueAnimator.setRepeatMode(ValueAnimator.RESTART);

        } else if (ValueAnimator.REVERSE == SetAnimRepeatMode()) {
            valueAnimator.setRepeatMode(ValueAnimator.REVERSE);

        }

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                OnAnimationUpdate(valueAnimator);

            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

            }

            @Override
            public void onAnimationStart(Animator animation) {

                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                OnAnimationRepeat(animation);
            }
        });
        if (!valueAnimator.isRunning()) {
            AnimIsRunning();
            valueAnimator.start();

        }

        return valueAnimator;
    }


    protected abstract void InitPaint();

    protected abstract void OnAnimationUpdate(ValueAnimator valueAnimator);

    protected abstract void OnAnimationRepeat(Animator animation);

    protected abstract int OnStopAnim();

    protected abstract int SetAnimRepeatMode();

    protected abstract int SetAnimRepeatCount();

    protected abstract void AnimIsRunning();


    public float getFontlength(Paint paint, String str) {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.width();
    }

    public float getFontHeight(Paint paint, String str) {
        Rect rect = new Rect();
        paint.getTextBounds(str, 0, str.length(), rect);
        return rect.height();

    }

    public float getFontHeight(Paint paint) {
        Paint.FontMetrics fm = paint.getFontMetrics();
        return fm.descent - fm.ascent;
    }
}