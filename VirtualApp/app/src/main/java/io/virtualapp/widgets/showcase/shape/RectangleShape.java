package io.virtualapp.widgets.showcase.shape;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import io.virtualapp.widgets.showcase.target.Target;

public class RectangleShape implements Shape {

    private boolean fullWidth = false;

    private int width = 0;
    private int height = 0;
    private boolean adjustToTarget = true;

    private Rect rect;

    public RectangleShape(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }

    public RectangleShape(Rect bounds) {
        this(bounds, false);
    }

    public RectangleShape(Rect bounds, boolean fullWidth) {
        this.fullWidth = fullWidth;
        height = bounds.height();
        if (fullWidth)
            width = Integer.MAX_VALUE;
        else width = bounds.width();
        init();
    }

    public boolean isAdjustToTarget() {
        return adjustToTarget;
    }

    public void setAdjustToTarget(boolean adjustToTarget) {
        this.adjustToTarget = adjustToTarget;
    }

    private void init() {
        rect = new Rect(- width / 2, - height / 2, width / 2, height / 2);
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y, int padding) {
        if (!rect.isEmpty()) {
            canvas.drawRect(
                    rect.left + x - padding,
                    rect.top + y - padding,
                    rect.right + x + padding,
                    rect.bottom + y + padding,
                    paint
            );
        }
    }

    @Override
    public void updateTarget(Target target) {
        if (adjustToTarget) {
            Rect bounds = target.getBounds();
            height = bounds.height();
            if (fullWidth)
                width = Integer.MAX_VALUE;
            else width = bounds.width();
            init();
        }
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}