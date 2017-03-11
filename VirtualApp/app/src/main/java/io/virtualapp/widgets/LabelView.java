package io.virtualapp.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

import io.virtualapp.R;

public class LabelView extends View {
    private static final int DEFAULT_DEGREES = 45;
    private String mTextContent;
    private int mTextColor;
    private float mTextSize;
    private boolean mTextBold;
    private boolean mFillTriangle;
    private boolean mTextAllCaps;
    private int mBackgroundColor;
    private float mMinSize;
    private float mPadding;
    private int mGravity;
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Path mPath = new Path();

    public LabelView(Context context) {
        this(context, null);
    }

    public LabelView(Context context, AttributeSet attrs) {
        super(context, attrs);

        obtainAttributes(context, attrs);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void obtainAttributes(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.LabelView);
        mTextContent = ta.getString(R.styleable.LabelView_lv_text);
        mTextColor = ta.getColor(R.styleable.LabelView_lv_text_color, Color.parseColor("#ffffff"));
        mTextSize = ta.getDimension(R.styleable.LabelView_lv_text_size, sp2px(11));
        mTextBold = ta.getBoolean(R.styleable.LabelView_lv_text_bold, true);
        mTextAllCaps = ta.getBoolean(R.styleable.LabelView_lv_text_all_caps, true);
        mFillTriangle = ta.getBoolean(R.styleable.LabelView_lv_fill_triangle, false);
        mBackgroundColor = ta.getColor(R.styleable.LabelView_lv_background_color, Color.parseColor("#FF4081"));
        mMinSize = ta.getDimension(R.styleable.LabelView_lv_min_size, mFillTriangle ? dp2px(35) : dp2px(50));
        mPadding = ta.getDimension(R.styleable.LabelView_lv_padding, dp2px(3.5f));
        mGravity = ta.getInt(R.styleable.LabelView_lv_gravity, Gravity.TOP | Gravity.LEFT);
        ta.recycle();
    }

    public String getText() {
        return mTextContent;
    }

    public void setText(String text) {
        mTextContent = text;
        invalidate();
    }

    public int getTextColor() {
        return mTextColor;
    }

    public void setTextColor(int textColor) {
        mTextColor = textColor;
        invalidate();
    }

    public float getTextSize() {
        return mTextSize;
    }

    public void setTextSize(float textSize) {
        mTextSize = sp2px(textSize);
        invalidate();
    }

    public boolean isTextBold() {
        return mTextBold;
    }

    public void setTextBold(boolean textBold) {
        mTextBold = textBold;
        invalidate();
    }

    public boolean isFillTriangle() {
        return mFillTriangle;
    }

    public void setFillTriangle(boolean fillTriangle) {
        mFillTriangle = fillTriangle;
        invalidate();
    }

    public boolean isTextAllCaps() {
        return mTextAllCaps;
    }

    public void setTextAllCaps(boolean textAllCaps) {
        mTextAllCaps = textAllCaps;
        invalidate();
    }

    public int getBgColor() {
        return mBackgroundColor;
    }

    public void setBgColor(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        invalidate();
    }

    public float getMinSize() {
        return mMinSize;
    }

    public void setMinSize(float minSize) {
        mMinSize = dp2px(minSize);
        invalidate();
    }

    public float getPadding() {
        return mPadding;
    }

    public void setPadding(float padding) {
        mPadding = dp2px(padding);
        invalidate();
    }

    public int getGravity() {
        return mGravity;
    }

    /**
     * Gravity.TOP | Gravity.LEFT
     * Gravity.TOP | Gravity.RIGHT
     * Gravity.BOTTOM | Gravity.LEFT
     * Gravity.BOTTOM | Gravity.RIGHT
     */
    public void setGravity(int gravity) {
        mGravity = gravity;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int size = getHeight();

        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setFakeBoldText(mTextBold);
        mBackgroundPaint.setColor(mBackgroundColor);

        float textHeight = mTextPaint.descent() - mTextPaint.ascent();
        if (mFillTriangle) {
            if (mGravity == (Gravity.TOP | Gravity.LEFT)) {
                mPath.reset();
                mPath.moveTo(0, 0);
                mPath.lineTo(0, size);
                mPath.lineTo(size, 0);
                mPath.close();
                canvas.drawPath(mPath, mBackgroundPaint);

                drawTextWhenFill(size, -DEFAULT_DEGREES, canvas, true);
            } else if (mGravity == (Gravity.TOP | Gravity.RIGHT)) {
                mPath.reset();
                mPath.moveTo(size, 0);
                mPath.lineTo(0, 0);
                mPath.lineTo(size, size);
                mPath.close();
                canvas.drawPath(mPath, mBackgroundPaint);

                drawTextWhenFill(size, DEFAULT_DEGREES, canvas, true);
            } else if (mGravity == (Gravity.BOTTOM | Gravity.LEFT)) {
                mPath.reset();
                mPath.moveTo(0, size);
                mPath.lineTo(0, 0);
                mPath.lineTo(size, size);
                mPath.close();
                canvas.drawPath(mPath, mBackgroundPaint);

                drawTextWhenFill(size, DEFAULT_DEGREES, canvas, false);
            } else if (mGravity == (Gravity.BOTTOM | Gravity.RIGHT)) {
                mPath.reset();
                mPath.moveTo(size, size);
                mPath.lineTo(0, size);
                mPath.lineTo(size, 0);
                mPath.close();
                canvas.drawPath(mPath, mBackgroundPaint);

                drawTextWhenFill(size, -DEFAULT_DEGREES, canvas, false);
            }
        } else {
            double delta = (textHeight + mPadding * 2) * Math.sqrt(2);
            if (mGravity == (Gravity.TOP | Gravity.LEFT)) {
                mPath.reset();
                mPath.moveTo(0, (float) (size - delta));
                mPath.lineTo(0, size);
                mPath.lineTo(size, 0);
                mPath.lineTo((float) (size - delta), 0);
                mPath.close();
                canvas.drawPath(mPath, mBackgroundPaint);

                drawText(size, -DEFAULT_DEGREES, canvas, textHeight, true);
            } else if (mGravity == (Gravity.TOP | Gravity.RIGHT)) {
                mPath.reset();
                mPath.moveTo(0, 0);
                mPath.lineTo((float) delta, 0);
                mPath.lineTo(size, (float) (size - delta));
                mPath.lineTo(size, size);
                mPath.close();
                canvas.drawPath(mPath, mBackgroundPaint);

                drawText(size, DEFAULT_DEGREES, canvas, textHeight, true);
            } else if (mGravity == (Gravity.BOTTOM | Gravity.LEFT)) {
                mPath.reset();
                mPath.moveTo(0, 0);
                mPath.lineTo(0, (float) delta);
                mPath.lineTo((float) (size - delta), size);
                mPath.lineTo(size, size);
                mPath.close();
                canvas.drawPath(mPath, mBackgroundPaint);

                drawText(size, DEFAULT_DEGREES, canvas, textHeight, false);
            } else if (mGravity == (Gravity.BOTTOM | Gravity.RIGHT)) {
                mPath.reset();
                mPath.moveTo(0, size);
                mPath.lineTo((float) delta, size);
                mPath.lineTo(size, (float) delta);
                mPath.lineTo(size, 0);
                mPath.close();
                canvas.drawPath(mPath, mBackgroundPaint);

                drawText(size, -DEFAULT_DEGREES, canvas, textHeight, false);
            }
        }
    }

    private void drawText(int size, float degrees, Canvas canvas, float textHeight, boolean isTop) {
        canvas.save();
        canvas.rotate(degrees, size / 2f, size / 2f);
        float delta = isTop ? -(textHeight + mPadding * 2) / 2 : (textHeight + mPadding * 2) / 2;
        float textBaseY = size / 2 - (mTextPaint.descent() + mTextPaint.ascent()) / 2 + delta;
        canvas.drawText(mTextAllCaps ? mTextContent.toUpperCase() : mTextContent,
                getPaddingLeft() + (size - getPaddingLeft() - getPaddingRight()) / 2, textBaseY, mTextPaint);
        canvas.restore();
    }

    private void drawTextWhenFill(int size, float degrees, Canvas canvas, boolean isTop) {
        canvas.save();
        canvas.rotate(degrees, size / 2f, size / 2f);
        float delta = isTop ? -size / 4 : size / 4;
        float textBaseY = size / 2 - (mTextPaint.descent() + mTextPaint.ascent()) / 2 + delta;
        canvas.drawText(mTextAllCaps ? mTextContent.toUpperCase() : mTextContent,
                getPaddingLeft() + (size - getPaddingLeft() - getPaddingRight()) / 2, textBaseY, mTextPaint);
        canvas.restore();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = measureWidth(widthMeasureSpec);
        setMeasuredDimension(measuredWidth, measuredWidth);
    }

    /**
     * 确定View宽度大小
     */
    private int measureWidth(int widthMeasureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        if (specMode == MeasureSpec.EXACTLY) {//大小确定直接使用
            result = specSize;
        } else {
            int padding = getPaddingLeft() + getPaddingRight();
            mTextPaint.setColor(mTextColor);
            mTextPaint.setTextSize(mTextSize);
            float textWidth = mTextPaint.measureText(mTextContent + "");
            result = (int) ((padding + (int) textWidth) * Math.sqrt(2));
            //如果父视图的测量要求为AT_MOST,即限定了一个最大值,则再从系统建议值和自己计算值中去一个较小值
            if (specMode == MeasureSpec.AT_MOST) {
                result = Math.min(result, specSize);
            }

            result = Math.max((int) mMinSize, result);
        }

        return result;
    }

    protected int dp2px(float dp) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected int sp2px(float sp) {
        final float scale = getResources().getDisplayMetrics().scaledDensity;
        return (int) (sp * scale + 0.5f);
    }
}