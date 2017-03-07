package io.virtualapp.home.adapters.decorations;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntDef;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This class can only be used in the RecyclerView which use a GridLayoutManager
 * or StaggeredGridLayoutManager, but it's not always work for the StaggeredGridLayoutManager,
 * because we can't figure out which position should belong to the last column or the last row
 */
public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {
    public static final int GRID_DIVIDER_HORIZONTAL = GridLayoutManager.HORIZONTAL;
    public static final int GRID_DIVIDER_VERTICAL = GridLayoutManager.VERTICAL;
    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };
    private final SparseIntArray mHorizontalDividerOffsets = new SparseIntArray();
    private final SparseIntArray mVerticalDividerOffsets = new SparseIntArray();

    private final SparseArray<DrawableCreator> mTypeDrawableFactories = new SparseArray<>();
    @Orientation
    private int mOrientation;
    private Drawable mHorizontalDivider;
    private Drawable mVerticalDivider;
    public GridDividerItemDecoration(Context context, @Orientation int orientation) {
        resolveDivider(context);
        setOrientation(orientation);
    }

    private void resolveDivider(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mVerticalDivider = mHorizontalDivider = a.getDrawable(0);
        a.recycle();
    }

    public void setOrientation(int orientation) {
        this.mOrientation = orientation;
    }

    public void setVerticalDivider(Drawable verticalDivider) {
        this.mVerticalDivider = verticalDivider;
    }

    public void setHorizontalDivider(Drawable horizontalDivider) {
        this.mHorizontalDivider = horizontalDivider;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        drawHorizontalDividers(c, parent);
        drawVerticalDividers(c, parent);
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        final int spanCount = getSpanCount(parent);
        final int childCount = parent.getAdapter().getItemCount();
        final int adapterPosition = parent.getChildAdapterPosition(view);

        if (mHorizontalDividerOffsets.indexOfKey(adapterPosition) < 0) {
            mHorizontalDividerOffsets.put(adapterPosition, getHorizontalDivider(parent, adapterPosition).getIntrinsicHeight());
        }

        if (mVerticalDividerOffsets.indexOfKey(adapterPosition) < 0) {
            mVerticalDividerOffsets.put(adapterPosition, getVerticalDivider(parent, adapterPosition).getIntrinsicHeight());
        }

        outRect.set(0, 0, mHorizontalDividerOffsets.get(adapterPosition), mVerticalDividerOffsets.get(adapterPosition));

        if (isLastRow(adapterPosition, spanCount, childCount)) {
            outRect.bottom = 0;
        }

        if (isLastColumn(adapterPosition, spanCount, childCount)) {
            outRect.right = 0;
        }

    }

    private boolean isLastColumn(int position, int spanCount, int childCount) {
        if (mOrientation == GRID_DIVIDER_VERTICAL) {
            return (position + 1) % spanCount == 0;
        } else {
            int lastColumnCount = childCount % spanCount;
            lastColumnCount = lastColumnCount == 0 ? spanCount : lastColumnCount;
            return position >= childCount - lastColumnCount;
        }
    }

    private boolean isLastRow(int position, int spanCount, int childCount) {
        if (mOrientation == GRID_DIVIDER_VERTICAL) {
            int lastColumnCount = childCount % spanCount;
            lastColumnCount = lastColumnCount == 0 ? spanCount : lastColumnCount;
            return position >= childCount - lastColumnCount;
        } else {
            return (position + 1) % spanCount == 0;
        }
    }

    private int getSpanCount(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();

        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getSpanCount();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        } else {
            throw new UnsupportedOperationException("the GridDividerItemDecoration can only be used in " +
                    "the RecyclerView which use a GridLayoutManager or StaggeredGridLayoutManager");
        }
    }

    public void drawVerticalDividers(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final Drawable divider = getVerticalDivider(parent, params.getViewAdapterPosition());
            final int top = child.getBottom() + params.bottomMargin + Math.round(ViewCompat.getTranslationY(child));
            final int bottom = top + divider.getIntrinsicHeight();

            mVerticalDividerOffsets.put(params.getViewAdapterPosition(), divider.getIntrinsicHeight());

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }

    public void drawHorizontalDividers(Canvas c, RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final Drawable divider = getHorizontalDivider(parent, params.getViewAdapterPosition());
            final int left = child.getRight() + params.rightMargin + Math.round(ViewCompat.getTranslationX(child));
            final int right = left + divider.getIntrinsicHeight();

            mHorizontalDividerOffsets.put(params.getViewAdapterPosition(), divider.getIntrinsicHeight());

            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }

    private Drawable getVerticalDivider(RecyclerView parent, int adapterPosition) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        int itemType = adapter.getItemViewType(adapterPosition);
        DrawableCreator drawableCreator = mTypeDrawableFactories.get(itemType);

        if (drawableCreator != null) {
            return drawableCreator.createVertical(parent, adapterPosition);
        }

        return mVerticalDivider;
    }

    private Drawable getHorizontalDivider(RecyclerView parent, int adapterPosition) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        int itemType = adapter.getItemViewType(adapterPosition);
        DrawableCreator drawableCreator = mTypeDrawableFactories.get(itemType);

        if (drawableCreator != null) {
            return drawableCreator.createHorizontal(parent, adapterPosition);
        }

        return mHorizontalDivider;
    }

    public void registerTypeDrawable(int itemType, DrawableCreator drawableCreator) {
        mTypeDrawableFactories.put(itemType, drawableCreator);
    }

    @IntDef({
            GRID_DIVIDER_HORIZONTAL,
            GRID_DIVIDER_VERTICAL
    })
    @Retention(RetentionPolicy.SOURCE)
    private @interface Orientation {
    }

    public interface DrawableCreator {
        Drawable createVertical(RecyclerView parent, int adapterPosition);

        Drawable createHorizontal(RecyclerView parent, int adapterPosition);
    }

}