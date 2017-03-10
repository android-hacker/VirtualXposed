package io.virtualapp.widgets;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;

import java.util.ArrayList;

public class BallGridBeatIndicator extends Indicator {

    private static final int ALPHA = 255;

    private static final int[] ALPHAS = new int[]{ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA,
            ALPHA};

    @Override
    public void draw(Canvas canvas, Paint paint) {
        float circleSpacing = 4;
        float radius = (getWidth() - circleSpacing * 4) / 6;
        float x = getWidth() / 2 - (radius * 2 + circleSpacing);
        float y = getWidth() / 2 - (radius * 2 + circleSpacing);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                canvas.save();
                float translateX = x + (radius * 2) * j + circleSpacing * j;
                float translateY = y + (radius * 2) * i + circleSpacing * i;
                canvas.translate(translateX, translateY);
                paint.setAlpha(ALPHAS[3 * i + j]);
                canvas.drawCircle(0, 0, radius, paint);
                canvas.restore();
            }
        }
    }

    @Override
    public ArrayList<ValueAnimator> onCreateAnimators() {
        ArrayList<ValueAnimator> animators = new ArrayList<>();

        int[] durations = {960, 930, 1190, 1130, 1340, 940, 1200, 820, 1190};
        int[] delays = {360, 400, 680, 410, 710, -150, -120, 10, 320};

        for (int i = 0; i < 9; i++) {
            final int index = i;
            ValueAnimator alphaAnim = ValueAnimator.ofInt(255, 168, 255);
            alphaAnim.setDuration(durations[i]);
            alphaAnim.setRepeatCount(-1);
            alphaAnim.setStartDelay(delays[i]);
            addUpdateListener(alphaAnim, animation -> {
                ALPHAS[index] = (int) animation.getAnimatedValue();
                postInvalidate();
            });
            animators.add(alphaAnim);
        }
        return animators;
    }


}