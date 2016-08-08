package io.virtualapp.abs.percent;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.percent.PercentLayoutHelper;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @author Lody
 */
public class PercentLinearLayout extends LinearLayout {

	private PercentLayoutHelper mPercentLayoutHelper;

	public PercentLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		mPercentLayoutHelper = new PercentLayoutHelper(this);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		mPercentLayoutHelper.adjustChildren(widthMeasureSpec, heightMeasureSpec);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if (mPercentLayoutHelper.handleMeasuredStateTooSmall()) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		mPercentLayoutHelper.restoreOriginalParams();
	}

	@Override
	public LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	public static class LayoutParams extends LinearLayout.LayoutParams
			implements
				PercentLayoutHelper.PercentLayoutParams {
		private PercentLayoutHelper.PercentLayoutInfo mPercentLayoutInfo;

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
			mPercentLayoutInfo = PercentLayoutHelper.getPercentLayoutInfo(c, attrs);
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		@Override
		public PercentLayoutHelper.PercentLayoutInfo getPercentLayoutInfo() {
			return mPercentLayoutInfo;
		}

		@Override
		protected void setBaseAttributes(TypedArray a, int widthAttr, int heightAttr) {
			PercentLayoutHelper.fetchWidthAndHeight(this, a, widthAttr, heightAttr);
		}

	}

}