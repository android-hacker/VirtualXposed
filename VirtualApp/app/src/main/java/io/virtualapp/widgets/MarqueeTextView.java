package io.virtualapp.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

public class MarqueeTextView extends AppCompatTextView {

    private boolean isStop = false;

    public MarqueeTextView(Context context) {
        super(context);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public boolean isFocused() {
        if (this.isStop) {
            return super.isFocused();
        }
        return true;
    }

    public void stopScroll() {
        this.isStop = true;
    }

    public void start() {
        this.isStop = false;
    }

    protected void onDetachedFromWindow() {
        stopScroll();
        super.onDetachedFromWindow();
    }
}