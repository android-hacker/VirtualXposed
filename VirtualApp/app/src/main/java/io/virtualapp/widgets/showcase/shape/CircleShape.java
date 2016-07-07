package io.virtualapp.widgets.showcase.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import io.virtualapp.widgets.showcase.target.Target;

/**
 * Circular shape for target.
 */
public class CircleShape implements Shape {

    private int radius = 200;
    private boolean adjustToTarget = true;

    public CircleShape() {
    }

    public CircleShape(int radius) {
        this.radius = radius;
    }

    public CircleShape(Rect bounds) {
        this(getPreferredRadius(bounds));
    }

    public CircleShape(Target target) {
        this(target.getBounds());
    }

    public void setAdjustToTarget(boolean adjustToTarget) {
        this.adjustToTarget = adjustToTarget;
    }

    public boolean isAdjustToTarget() {
        return adjustToTarget;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y, int padding) {
        if (radius > 0) {
            canvas.drawCircle(x, y, radius + padding, paint);
        }
    }

    @Override
    public void updateTarget(Target target) {
        if (adjustToTarget)
            radius = getPreferredRadius(target.getBounds());
    }

    @Override
    public int getWidth() {
        return radius * 2;
    }

    @Override
    public int getHeight() {
        return radius * 2;
    }

    public static int getPreferredRadius(Rect bounds) {
        return Math.max(bounds.width(), bounds.height()) / 2;
    }
}
