package io.virtualapp.widgets.fittext;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.text.Layout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;


class BaseTextView extends TextView {
    protected boolean mSingleLine = false;
    protected boolean mIncludeFontPadding = true;
    protected float mLineSpacingMult = 1;
    protected float mLineSpacingAdd = 0;
    protected int mMaxLines = Integer.MAX_VALUE;
    protected boolean mLineEndNoSpace = true;
    protected boolean mJustify = false;

    /***
     * 不拆分单词
     */
    protected boolean mKeepWord = true;
    @SuppressWarnings("deprecation")
    private static final int[] ANDROID_ATTRS = new int[]{
            android.R.attr.includeFontPadding,
            android.R.attr.lineSpacingMultiplier,
            android.R.attr.lineSpacingExtra,
            android.R.attr.maxLines,
            android.R.attr.singleLine,
            };

    public BaseTextView(Context context) {
        this(context, null);
    }

    public BaseTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, ANDROID_ATTRS);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                mIncludeFontPadding = a.getBoolean(a.getIndex(0),  mIncludeFontPadding);
                mLineSpacingMult = a.getFloat(a.getIndex(1),  mLineSpacingMult);
                mLineSpacingAdd = a.getDimensionPixelSize(a.getIndex(2),  (int) mLineSpacingAdd);
                mMaxLines = a.getInteger(a.getIndex(3), mMaxLines);
            }
            mSingleLine = a.getBoolean(android.R.attr.singleLine, mSingleLine);
            a.recycle();
        }
    }

    public BaseTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
    }

    public boolean isKeepWord() {
        return mKeepWord;
    }

    public void setKeepWord(boolean keepWord) {
        mKeepWord = keepWord;
    }

    public boolean isJustify() {
        return mJustify;
    }

    public void setJustify(boolean justify) {
        mJustify = justify;
    }

    public boolean isLineEndNoSpace() {
        return mLineEndNoSpace;
    }

    public void setLineEndNoSpace(boolean lineEndNoSpace) {
        mLineEndNoSpace = lineEndNoSpace;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public boolean getIncludeFontPaddingCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return getIncludeFontPadding();
        } else {
            return mIncludeFontPadding;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public float getLineSpacingMultiplierCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return getLineSpacingMultiplier();
        } else {
            return mLineSpacingMult;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public float getLineSpacingExtraCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return getLineSpacingExtra();
        } else {
            return mLineSpacingAdd;
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public int getMaxLinesCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return getMaxLines();
        } else {
            return mMaxLines;
        }
    }

    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mLineSpacingAdd = add;
        mLineSpacingMult = mult;
    }

    @Override
    public void setIncludeFontPadding(boolean includepad) {
        super.setIncludeFontPadding(includepad);
        mIncludeFontPadding = includepad;
    }

    @Override
    public void setMaxLines(int maxlines) {
        super.setMaxLines(maxlines);
        mMaxLines = maxlines;
    }

    @Override
    public void setSingleLine(boolean singleLine) {
        super.setSingleLine(singleLine);
        mSingleLine = singleLine;
    }


    public int getTextWidth() {
        return FitTextHelper.getTextWidth(this);
    }

    public int getTextHeight() {
        return getMeasuredHeight() - getCompoundPaddingTop()
                - getCompoundPaddingBottom();
    }

    /**
     * 设置粗体
     *
     * @param bold 粗体
     */
    public void setBoldText(boolean bold) {
        getPaint().setFakeBoldText(bold);
    }

    /**
     * 设置斜体
     *
     * @param italic 斜体
     */
    public void setItalicText(boolean italic) {
        getPaint().setTextSkewX(italic ? -0.25f : 0f);
    }

    public boolean isItalicText() {
        return getPaint().getTextSkewX() != 0f;
    }

    public boolean isSingleLine() {
        return mSingleLine;
    }

    public float getTextLineHeight() {
        return getLineHeight();
    }

    public TextView getTextView() {
        return this;
    }

    protected void onDraw(Canvas canvas) {
        if (!mJustify || mSingleLine) {
            super.onDraw(canvas);
            return;
        }
        TextPaint paint = getPaint();
//        paint.drawableState = getDrawableState();
        float mViewWidth = getTextWidth();
        if (isItalicText()) {
            float letterW = getPaint().measureText("a");
            mViewWidth -= letterW;
        }
        CharSequence text = getText();
        Layout layout = getLayout();
        if (layout == null) {
            layout = FitTextHelper.getStaticLayout(this, getText(), getPaint());
        }
        int count = layout.getLineCount();
        for (int i = 0; i < count; i++) {
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
//            int top = layout.getLineTop(i);
            float x = layout.getLineLeft(i);
            int mLineY = layout.getTopPadding() + (i + 1) * getLineHeight();
            CharSequence line = text.subSequence(lineStart, lineEnd);
            if (line.length() == 0) {
                continue;
            }
            if (mLineEndNoSpace) {
                if (TextUtils.equals(line.subSequence(line.length() - 1, line.length()), " ")) {
                    line = line.subSequence(0, line.length() - 1);
                }
                if (TextUtils.equals(line.subSequence(0, 1), " ")) {
                    line = line.subSequence(1, line.length() - 1);
                }
            }
            float lineWidth = getPaint().measureText(text, lineStart, lineEnd);
            boolean needScale = i < (count - 1) && (needScale(text.subSequence(lineEnd - 1, lineEnd)));
//            if (i < (count - 1) && needScale(line)) {

            //float width = StaticLayout.getDesiredWidth(text, lineStart, lineEnd, getPaint());
//                drawScaledText(canvas, mViewWidth, mLineY, lineStart, line, width - getCompoundPaddingLeft() - getCompoundPaddingRight());
//            } else {
//                canvas.drawText(line, 0, line.length(), 0, mLineY, paint);
//            }
//            float x = getCompoundPaddingLeft();
            if (needScale && mViewWidth > lineWidth) {
//                float sc = mViewWidth / lineWidth;
                //标点数
                int clen = countEmpty(line);
                float d = (mViewWidth - lineWidth) / clen;
                for (int j = 0; j < line.length(); j++) {
                    float cw = getPaint().measureText(line, j, j + 1);
                    canvas.drawText(line, j, j + 1, x, mLineY, getPaint());
                    x += cw;
                    // 后面是标点
                    if (isEmpty(line, j + 1, j + 2)) {
                        x += d / 2;
                    }
                    //当前是标点
                    if (isEmpty(line, j, j + 1)) {
                        x += d / 2;
                    }
                }
            } else {
                canvas.drawText(line, 0, line.length(), x, mLineY, paint);
            }
        }
    }

    /**
     * 共有多少个标点/空白字符
     *
     * @param text 内容
     * @return 数量
     */
    protected int countEmpty(CharSequence text) {
        int len = text.length();
        int count = 0;
        for (int i = 0; i < len; i++) {
            if (isEmpty(text, i, i + 1)) {
                count++;
            }
        }
        return count;
    }

    /**
     * 是否是标点/空白字符
     *
     * @param c     内容
     * @param start 开始
     * @param end   结束
     */
    protected boolean isEmpty(CharSequence c, int start, int end) {
        if (end >= c.length()) {
            return false;
        }
        CharSequence ch = c.subSequence(start, end);
        return TextUtils.equals(ch, "\t") || TextUtils.equals(ch, " ") || FitTextHelper.sSpcaeList.contains(ch);
    }

//    private void drawScaledText(Canvas canvas, int mViewWidth, int mLineY, int lineStart, CharSequence line, float lineWidth) {
//        float x = 0;
//        if (isFirstLineOfParagraph(lineStart, line)) {
//            String blanks = "  ";
//            canvas.drawText(blanks, x, mLineY, getPaint());
//            float bw = StaticLayout.getDesiredWidth(blanks, getPaint());
//            x += bw;
//
//            line = line.subSequence(3, line.length() - 3);
//        }
//
//        float d = (mViewWidth - lineWidth) / line.length() - 1;
//        for (int i = 0; i < line.length(); i++) {
//            String c = String.valueOf(line.charAt(i));
//            float cw = StaticLayout.getDesiredWidth(c, getPaint());
//            canvas.drawText(c, x, mLineY, getPaint());
//            x += cw + d;
//        }
//    }
//
//    private boolean isFirstLineOfParagraph(int lineStart, CharSequence line) {
//        return line.length() > 3 && line.charAt(0) == ' ' && line.charAt(1) == ' ';
//    }

    /**
     * 是否需要两端对齐
     *
     * @param end 结束字符
     */
    protected boolean needScale(CharSequence end) {
        return TextUtils.equals(end, " ");// || !TextUtils.equals(end, "\n");
    }

}