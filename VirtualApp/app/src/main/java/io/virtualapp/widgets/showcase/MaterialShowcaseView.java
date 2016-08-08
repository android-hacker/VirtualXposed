package io.virtualapp.widgets.showcase;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import io.virtualapp.R;
import io.virtualapp.widgets.showcase.shape.CircleShape;
import io.virtualapp.widgets.showcase.shape.NoShape;
import io.virtualapp.widgets.showcase.shape.RectangleShape;
import io.virtualapp.widgets.showcase.shape.Shape;
import io.virtualapp.widgets.showcase.target.Target;
import io.virtualapp.widgets.showcase.target.ViewTarget;

/**
 * Helper class to show a sequence of showcase views.
 */
public class MaterialShowcaseView extends FrameLayout implements View.OnTouchListener, View.OnClickListener {

										// mode
	List<IShowcaseListener> mListeners; // external listeners who want to
	private int mOldHeight;
	private int mOldWidth;
	private Bitmap mBitmap;// = new WeakReference<>(null);
	private Canvas mCanvas;
	private Paint mEraser;
	private Target mTarget;
	private Shape mShape;
	private int mXPosition;
	private int mYPosition;
	private boolean mWasDismissed = false;
	private int mShapePadding = ShowcaseConfig.DEFAULT_SHAPE_PADDING;
	private View mContentBox;
	private TextView mTitleTextView;
	private TextView mContentTextView;
	private TextView mDismissButton;
	private int mGravity;
	private int mContentBottomMargin;
	private int mContentTopMargin;
	private boolean mDismissOnTouch = false;
	private boolean mShouldRender = false; // flag to decide when we should
											// actually render
	private boolean mRenderOverNav = false;
	private int mMaskColour;
	private AnimationFactory mAnimationFactory;
	private boolean mShouldAnimate = true;
	private long mFadeDurationInMillis = ShowcaseConfig.DEFAULT_FADE_TIME;
	private Handler mHandler;
	private long mDelayInMillis = ShowcaseConfig.DEFAULT_DELAY;
	private int mBottomMargin = 0;
	private boolean mSingleUse = false; // should display only once
	private PrefsManager mPrefsManager; // used to store state doe single use
										// observe when we show and dismiss
	private UpdateOnGlobalLayout mLayoutListener;
	private IDetachedListener mDetachedListener;
	private boolean mTargetTouchable = false;
	private boolean mDismissOnTargetTouch = true;

	public MaterialShowcaseView(Context context) {
		super(context);
		init(context);
	}

	public MaterialShowcaseView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public MaterialShowcaseView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public MaterialShowcaseView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context);
	}

	/**
	 * Static helper method for resetting single use flag
	 *
	 * @param context
	 * @param showcaseID
	 */
	public static void resetSingleUse(Context context, String showcaseID) {
		PrefsManager.resetShowcase(context, showcaseID);
	}

	/**
	 * Static helper method for resetting all single use flags
	 *
	 * @param context
	 */
	public static void resetAll(Context context) {
		PrefsManager.resetAll(context);
	}

	public static int getSoftButtonsBarSizePort(Activity activity) {
		// getRealMetrics is only available with API 17 and +
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
			DisplayMetrics metrics = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
			int usableHeight = metrics.heightPixels;
			activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
			int realHeight = metrics.heightPixels;
			if (realHeight > usableHeight)
				return realHeight - usableHeight;
			else
				return 0;
		}
		return 0;
	}

	private void init(Context context) {
		setWillNotDraw(false);

		// create our animation factory
		mAnimationFactory = new AnimationFactory();

		mListeners = new ArrayList<>();

		// make sure we add a global layout listener so we can adapt to changes
		mLayoutListener = new UpdateOnGlobalLayout();
		getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);

		// consume touch events
		setOnTouchListener(this);

		mMaskColour = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR);
		setVisibility(INVISIBLE);

		View contentView = LayoutInflater.from(getContext()).inflate(R.layout.showcase_content, this, true);
		mContentBox = contentView.findViewById(R.id.content_box);
		mTitleTextView = (TextView) contentView.findViewById(R.id.tv_title);
		mContentTextView = (TextView) contentView.findViewById(R.id.tv_content);
		mDismissButton = (TextView) contentView.findViewById(R.id.tv_dismiss);
		mDismissButton.setOnClickListener(this);
	}

	/**
	 * Interesting drawing stuff. We draw a block of semi transparent colour to
	 * fill the whole screen then we draw of transparency to create a circular
	 * "viewport" through to the underlying content
	 *
	 * @param canvas
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		// don't bother drawing if we're not ready
		if (!mShouldRender)
			return;

		// get current dimensions
		final int width = getMeasuredWidth();
		final int height = getMeasuredHeight();

		// don't bother drawing if there is nothing to draw on
		if (width <= 0 || height <= 0)
			return;

		// build a new canvas if needed i.e first pass or new dimensions
		if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {

			if (mBitmap != null)
				mBitmap.recycle();

			mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

			mCanvas = new Canvas(mBitmap);
		}

		// save our 'old' dimensions
		mOldWidth = width;
		mOldHeight = height;

		// clear canvas
		mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

		// draw solid background
		mCanvas.drawColor(mMaskColour);

		// Prepare eraser Paint if needed
		if (mEraser == null) {
			mEraser = new Paint();
			mEraser.setColor(0xFFFFFFFF);
			mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
			mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
		}

		// draw (erase) shape
		mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding);

		// Draw the bitmap on our views canvas.
		canvas.drawBitmap(mBitmap, 0, 0, null);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		/**
		 * If we're being detached from the window without the mWasDismissed
		 * flag then we weren't purposefully dismissed Probably due to an
		 * orientation change or user backed out of activity. Ensure we reset
		 * the flag so the showcase display again.
		 */
		if (!mWasDismissed && mSingleUse && mPrefsManager != null) {
			mPrefsManager.resetShowcase();
		}

		notifyOnDismissed();

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mDismissOnTouch) {
			hide();
		}
		if (mTargetTouchable && mTarget.getBounds().contains((int) event.getX(), (int) event.getY())) {
			if (mDismissOnTargetTouch) {
				hide();
			}
			return false;
		}
		return true;
	}

	private void notifyOnDisplayed() {

		if (mListeners != null) {
			for (IShowcaseListener listener : mListeners) {
				listener.onShowcaseDisplayed(this);
			}
		}
	}

	private void notifyOnDismissed() {
		if (mListeners != null) {
			for (IShowcaseListener listener : mListeners) {
				listener.onShowcaseDismissed(this);
			}

			mListeners.clear();
			mListeners = null;
		}

		/**
		 * internal listener used by sequence for storing progress within the
		 * sequence
		 */
		if (mDetachedListener != null) {
			mDetachedListener.onShowcaseDetached(this, mWasDismissed);
		}
	}

	/**
	 * Dismiss button clicked
	 *
	 * @param v
	 */
	@Override
	public void onClick(View v) {
		hide();
	}

	/**
	 * Tells us about the "Target" which is the view we want to anchor to. We
	 * figure out where it is on screen and (optionally) how big it is. We also
	 * figure out whether to place our content and dismiss button above or below
	 * it.
	 *
	 * @param target
	 */
	public void setTarget(Target target) {
		mTarget = target;

		// update dismiss button state
		updateDismissButton();

		if (mTarget != null) {

			/**
			 * If we're on lollipop then make sure we don't draw over the nav
			 * bar
			 */
			if (!mRenderOverNav && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				mBottomMargin = getSoftButtonsBarSizePort((Activity) getContext());
				LayoutParams contentLP = (LayoutParams) getLayoutParams();

				if (contentLP != null && contentLP.bottomMargin != mBottomMargin)
					contentLP.bottomMargin = mBottomMargin;
			}

			// apply the target position
			Point targetPoint = mTarget.getPoint();
			Rect targetBounds = mTarget.getBounds();
			setPosition(targetPoint);

			// now figure out whether to put content above or below it
			int height = getMeasuredHeight();
			int midPoint = height / 2;
			int yPos = targetPoint.y;

			int radius = Math.max(targetBounds.height(), targetBounds.width()) / 2;
			if (mShape != null) {
				mShape.updateTarget(mTarget);
				radius = mShape.getHeight() / 2;
			}

			if (yPos > midPoint) {
				// target is in lower half of screen, we'll sit above it
				mContentTopMargin = 0;
				mContentBottomMargin = (height - yPos) + radius + mShapePadding;
				mGravity = Gravity.BOTTOM;
			} else {
				// target is in upper half of screen, we'll sit below it
				mContentTopMargin = yPos + radius + mShapePadding;
				mContentBottomMargin = 0;
				mGravity = Gravity.TOP;
			}
		}

		applyLayoutParams();
	}

	private void applyLayoutParams() {

		if (mContentBox != null && mContentBox.getLayoutParams() != null) {
			LayoutParams contentLP = (LayoutParams) mContentBox.getLayoutParams();

			boolean layoutParamsChanged = false;

			if (contentLP.bottomMargin != mContentBottomMargin) {
				contentLP.bottomMargin = mContentBottomMargin;
				layoutParamsChanged = true;
			}

			if (contentLP.topMargin != mContentTopMargin) {
				contentLP.topMargin = mContentTopMargin;
				layoutParamsChanged = true;
			}

			if (contentLP.gravity != mGravity) {
				contentLP.gravity = mGravity;
				layoutParamsChanged = true;
			}

			/**
			 * Only apply the layout params if we've actually changed them,
			 * otherwise we'll get stuck in a layout loop
			 */
			if (layoutParamsChanged)
				mContentBox.setLayoutParams(contentLP);
		}
	}

	/**
	 * SETTERS
	 */

	void setPosition(Point point) {
		setPosition(point.x, point.y);
	}

	void setPosition(int x, int y) {
		mXPosition = x;
		mYPosition = y;
	}

	private void setTitleText(CharSequence contentText) {
		if (mTitleTextView != null && !contentText.equals("")) {
			mContentTextView.setAlpha(0.5F);
			mTitleTextView.setText(contentText);
		}
	}

	private void setContentText(CharSequence contentText) {
		if (mContentTextView != null) {
			mContentTextView.setText(contentText);
		}
	}

	private void setDismissText(CharSequence dismissText) {
		if (mDismissButton != null) {
			mDismissButton.setText(dismissText);

			updateDismissButton();
		}
	}

	private void setTitleTextColor(int textColour) {
		if (mTitleTextView != null) {
			mTitleTextView.setTextColor(textColour);
		}
	}

	private void setContentTextColor(int textColour) {
		if (mContentTextView != null) {
			mContentTextView.setTextColor(textColour);
		}
	}

	private void setDismissTextColor(int textColour) {
		if (mDismissButton != null) {
			mDismissButton.setTextColor(textColour);
		}
	}

	private void setShapePadding(int padding) {
		mShapePadding = padding;
	}

	private void setDismissOnTouch(boolean dismissOnTouch) {
		mDismissOnTouch = dismissOnTouch;
	}

	private void setShouldRender(boolean shouldRender) {
		mShouldRender = shouldRender;
	}

	private void setMaskColour(int maskColour) {
		mMaskColour = maskColour;
	}

	private void setDelay(long delayInMillis) {
		mDelayInMillis = delayInMillis;
	}

	private void setFadeDuration(long fadeDurationInMillis) {
		mFadeDurationInMillis = fadeDurationInMillis;
	}

	private void setTargetTouchable(boolean targetTouchable) {
		mTargetTouchable = targetTouchable;
	}

	private void setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
		mDismissOnTargetTouch = dismissOnTargetTouch;
	}

	public void addShowcaseListener(IShowcaseListener showcaseListener) {

		if (mListeners != null)
			mListeners.add(showcaseListener);
	}

	public void removeShowcaseListener(MaterialShowcaseSequence showcaseListener) {

		if ((mListeners != null) && mListeners.contains(showcaseListener)) {
			mListeners.remove(showcaseListener);
		}
	}

	void setDetachedListener(IDetachedListener detachedListener) {
		mDetachedListener = detachedListener;
	}

	public void setShape(Shape mShape) {
		this.mShape = mShape;
	}

	/**
	 * Set properties based on a config object
	 *
	 * @param config
	 */
	public void setConfig(ShowcaseConfig config) {
		setDelay(config.getDelay());
		setFadeDuration(config.getFadeDuration());
		setContentTextColor(config.getContentTextColor());
		setDismissTextColor(config.getDismissTextColor());
		setMaskColour(config.getMaskColor());
		setShape(config.getShape());
		setShapePadding(config.getShapePadding());
		setRenderOverNavigationBar(config.getRenderOverNavigationBar());
	}

	private void updateDismissButton() {
		// hide or show button
		if (mDismissButton != null) {
			if (TextUtils.isEmpty(mDismissButton.getText())) {
				mDismissButton.setVisibility(GONE);
			} else {
				mDismissButton.setVisibility(VISIBLE);
			}
		}
	}

	public boolean hasFired() {
		return mPrefsManager.hasFired();
	}

	private void singleUse(String showcaseID) {
		mSingleUse = true;
		mPrefsManager = new PrefsManager(getContext(), showcaseID);
	}

	public void removeFromWindow() {
		if (getParent() != null && getParent() instanceof ViewGroup) {
			((ViewGroup) getParent()).removeView(this);
		}

		if (mBitmap != null) {
			mBitmap.recycle();
			mBitmap = null;
		}

		mEraser = null;
		mAnimationFactory = null;
		mCanvas = null;
		mHandler = null;

		getViewTreeObserver().removeGlobalOnLayoutListener(mLayoutListener);
		mLayoutListener = null;

		if (mPrefsManager != null)
			mPrefsManager.close();

		mPrefsManager = null;

	}

	/**
	 * Reveal the showcaseview. Returns a boolean telling us whether we actually
	 * did show anything
	 *
	 * @param activity
	 * @return
	 */
	public boolean show(final Activity activity) {

		/**
		 * if we're in single use mode and have already shot our bolt then do
		 * nothing
		 */
		if (mSingleUse) {
			if (mPrefsManager.hasFired()) {
				return false;
			} else {
				mPrefsManager.setFired();
			}
		}

		((ViewGroup) activity.getWindow().getDecorView()).addView(this);

		setShouldRender(true);

		mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {

				if (mShouldAnimate) {
					fadeIn();
				} else {
					setVisibility(VISIBLE);
					notifyOnDisplayed();
				}
			}
		}, mDelayInMillis);

		updateDismissButton();

		return true;
	}

	public void hide() {

		/**
		 * This flag is used to indicate to onDetachedFromWindow that the
		 * showcase view was dismissed purposefully (by the user or
		 * programmatically)
		 */
		mWasDismissed = true;

		if (mShouldAnimate) {
			fadeOut();
		} else {
			removeFromWindow();
		}
	}

	public void fadeIn() {
		setVisibility(INVISIBLE);

		mAnimationFactory.fadeInView(this, mFadeDurationInMillis, new IAnimationFactory.AnimationStartListener() {
			@Override
			public void onAnimationStart() {
				setVisibility(View.VISIBLE);
				notifyOnDisplayed();
			}
		});
	}

	public void fadeOut() {

		mAnimationFactory.fadeOutView(this, mFadeDurationInMillis, new IAnimationFactory.AnimationEndListener() {
			@Override
			public void onAnimationEnd() {
				setVisibility(INVISIBLE);
				removeFromWindow();
			}
		});
	}

	public void resetSingleUse() {
		if (mSingleUse && mPrefsManager != null)
			mPrefsManager.resetShowcase();
	}

	private void setRenderOverNavigationBar(boolean mRenderOverNav) {
		this.mRenderOverNav = mRenderOverNav;
	}

	/**
	 * BUILDER CLASS Gives us a builder utility class with a fluent API for
	 * eaily configuring showcase views
	 */
	public static class Builder {
		private static final int CIRCLE_SHAPE = 0;
		private static final int RECTANGLE_SHAPE = 1;
		private static final int NO_SHAPE = 2;
		final MaterialShowcaseView showcaseView;
		private final Activity activity;
		private boolean fullWidth = false;
		private int shapeType = CIRCLE_SHAPE;

		public Builder(Activity activity) {
			this.activity = activity;

			showcaseView = new MaterialShowcaseView(activity);
		}

		/**
		 * Set the title text shown on the ShowcaseView.
		 */
		public Builder setTarget(View target) {
			showcaseView.setTarget(new ViewTarget(target));
			return this;
		}

		/**
		 * Set the title text shown on the ShowcaseView.
		 */
		public Builder setDismissText(int resId) {
			return setDismissText(activity.getString(resId));
		}

		public Builder setDismissText(CharSequence dismissText) {
			showcaseView.setDismissText(dismissText);
			return this;
		}

		/**
		 * Set the content text shown on the ShowcaseView.
		 */
		public Builder setContentText(int resId) {
			return setContentText(activity.getString(resId));
		}

		/**
		 * Set the descriptive text shown on the ShowcaseView.
		 */
		public Builder setContentText(CharSequence text) {
			showcaseView.setContentText(text);
			return this;
		}

		/**
		 * Set the title text shown on the ShowcaseView.
		 */
		public Builder setTitleText(int resId) {
			return setTitleText(activity.getString(resId));
		}

		/**
		 * Set the descriptive text shown on the ShowcaseView as the title.
		 */
		public Builder setTitleText(CharSequence text) {
			showcaseView.setTitleText(text);
			return this;
		}

		/**
		 * Set whether or not the target view can be touched while the showcase
		 * is visible.
		 *
		 * False by default.
		 */
		public Builder setTargetTouchable(boolean targetTouchable) {
			showcaseView.setTargetTouchable(targetTouchable);
			return this;
		}

		/**
		 * Set whether or not the showcase should dismiss when the target is
		 * touched.
		 *
		 * True by default.
		 */
		public Builder setDismissOnTargetTouch(boolean dismissOnTargetTouch) {
			showcaseView.setDismissOnTargetTouch(dismissOnTargetTouch);
			return this;
		}

		public Builder setDismissOnTouch(boolean dismissOnTouch) {
			showcaseView.setDismissOnTouch(dismissOnTouch);
			return this;
		}

		public Builder setMaskColour(int maskColour) {
			showcaseView.setMaskColour(maskColour);
			return this;
		}

		public Builder setTitleTextColor(int textColour) {
			showcaseView.setTitleTextColor(textColour);
			return this;
		}

		public Builder setContentTextColor(int textColour) {
			showcaseView.setContentTextColor(textColour);
			return this;
		}

		public Builder setDismissTextColor(int textColour) {
			showcaseView.setDismissTextColor(textColour);
			return this;
		}

		public Builder setDelay(int delayInMillis) {
			showcaseView.setDelay(delayInMillis);
			return this;
		}

		public Builder setFadeDuration(int fadeDurationInMillis) {
			showcaseView.setFadeDuration(fadeDurationInMillis);
			return this;
		}

		public Builder setListener(IShowcaseListener listener) {
			showcaseView.addShowcaseListener(listener);
			return this;
		}

		public Builder singleUse(String showcaseID) {
			showcaseView.singleUse(showcaseID);
			return this;
		}

		public Builder setShape(Shape shape) {
			showcaseView.setShape(shape);
			return this;
		}

		public Builder withCircleShape() {
			shapeType = CIRCLE_SHAPE;
			return this;
		}

		public Builder withoutShape() {
			shapeType = NO_SHAPE;
			return this;
		}

		public Builder setShapePadding(int padding) {
			showcaseView.setShapePadding(padding);
			return this;
		}

		public Builder withRectangleShape() {
			return withRectangleShape(false);
		}

		public Builder withRectangleShape(boolean fullWidth) {
			this.shapeType = RECTANGLE_SHAPE;
			this.fullWidth = fullWidth;
			return this;
		}

		public Builder renderOverNavigationBar() {
			// Note: This only has an effect in Lollipop or above.
			showcaseView.setRenderOverNavigationBar(true);
			return this;
		}

		public MaterialShowcaseView build() {
			if (showcaseView.mShape == null) {
				switch (shapeType) {
					case RECTANGLE_SHAPE : {
						showcaseView.setShape(new RectangleShape(showcaseView.mTarget.getBounds(), fullWidth));
						break;
					}
					case CIRCLE_SHAPE : {
						showcaseView.setShape(new CircleShape(showcaseView.mTarget));
						break;
					}
					case NO_SHAPE : {
						showcaseView.setShape(new NoShape());
						break;
					}
					default :
						throw new IllegalArgumentException("Unsupported shape type: " + shapeType);
				}
			}

			return showcaseView;
		}

		public MaterialShowcaseView show() {
			build().show(activity);
			return showcaseView;
		}

	}

	/**
	 * REDRAW LISTENER - this ensures we redraw after activity finishes laying
	 * out
	 */
	private class UpdateOnGlobalLayout implements ViewTreeObserver.OnGlobalLayoutListener {

		@Override
		public void onGlobalLayout() {
			setTarget(mTarget);
		}
	}
}
