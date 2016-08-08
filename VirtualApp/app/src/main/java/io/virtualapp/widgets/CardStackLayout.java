package io.virtualapp.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import io.virtualapp.R;

/**
 * Displays a list of cards as a stack on the screen.
 * <p>
 * <b>XML attributes</b>
 * <p>
 * See {@link R.styleable#CardStackLayout CardStackLayout Attributes}
 * <p>
 * {@link R.styleable#CardStackLayout_showInitAnimation}
 * {@link R.styleable#CardStackLayout_card_gap}
 * {@link R.styleable#CardStackLayout_card_gap_bottom}
 * {@link R.styleable#CardStackLayout_parallax_enabled}
 * {@link R.styleable#CardStackLayout_parallax_scale}
 */
public class CardStackLayout extends FrameLayout {
	public static final boolean PARALLAX_ENABLED_DEFAULT = false;
	public static final boolean SHOW_INIT_ANIMATION_DEFAULT = true;

	private float mCardGapBottom;
	private float mCardGap;
	private boolean mShowInitAnimation;
	private boolean mParallaxEnabled;
	private int mParallaxScale;
	private OnCardSelected mOnCardSelectedListener = null;

	private CardStackAdapter mAdapter = null;

	public CardStackLayout(Context context) {
		super(context);
		resetDefaults();
	}

	public CardStackLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CardStackLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		handleArgs(context, attrs, defStyleAttr, 0);
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public CardStackLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		handleArgs(context, attrs, defStyleAttr, defStyleRes);
	}

	/**
	 * package restricted
	 */
	OnCardSelected getOnCardSelectedListener() {
		return mOnCardSelectedListener;
	}

	/**
	 * Listen on card selection events for {@link CardStackLayout}. Sends
	 * clicked view and it's corresponding position in the callback.
	 *
	 * @param onCardSelectedListener
	 *            listener
	 */
	public void setOnCardSelectedListener(OnCardSelected onCardSelectedListener) {
		this.mOnCardSelectedListener = onCardSelectedListener;
	}

	private void resetDefaults() {
		mOnCardSelectedListener = null;
	}

	private void handleArgs(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		resetDefaults();

		final TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CardStackLayout, defStyleAttr,
				defStyleRes);
		mParallaxEnabled = a.getBoolean(R.styleable.CardStackLayout_parallax_enabled, PARALLAX_ENABLED_DEFAULT);
		mShowInitAnimation = a.getBoolean(R.styleable.CardStackLayout_showInitAnimation, SHOW_INIT_ANIMATION_DEFAULT);
		mParallaxScale = a.getInteger(R.styleable.CardStackLayout_parallax_scale,
				getResources().getInteger(R.integer.parallax_scale_default));
		mCardGap = a.getDimension(R.styleable.CardStackLayout_card_gap, getResources().getDimension(R.dimen.card_gap));
		mCardGapBottom = a.getDimension(R.styleable.CardStackLayout_card_gap_bottom,
				getResources().getDimension(R.dimen.card_gap_bottom));

		a.recycle();
	}

	/**
	 * @return adapter of type {@link CardStackAdapter} that is set for this
	 *         view.
	 */
	public CardStackAdapter getAdapter() {
		return mAdapter;
	}

	/**
	 * Set the adapter for this {@link CardStackLayout}
	 *
	 * @param adapter
	 *            Should extend {@link CardStackAdapter}
	 */
	public void setAdapter(CardStackAdapter adapter) {
		this.mAdapter = adapter;
		mAdapter.setAdapterParams(this);
		for (int i = 0; i < mAdapter.getCount(); i++) {
			mAdapter.addView(i);
		}

		if (mShowInitAnimation) {
			postDelayed(this::restoreCards, 500);
		}
	}

	/**
	 * @return currently set parallax scale value.
	 */
	public int getParallaxScale() {
		return mParallaxScale;
	}

	/**
	 * Sets the value of parallax scale. Parallax scale is the factor which
	 * decides how much distance a card will scroll when the user drags it down.
	 */
	public void setParallaxScale(int mParallaxScale) {
		this.mParallaxScale = mParallaxScale;
	}

	public boolean isParallaxEnabled() {
		return mParallaxEnabled;
	}

	public void setParallaxEnabled(boolean mParallaxEnabled) {
		this.mParallaxEnabled = mParallaxEnabled;
	}

	public boolean isShowInitAnimation() {
		return mShowInitAnimation;
	}

	public void setShowInitAnimation(boolean mShowInitAnimation) {
		this.mShowInitAnimation = mShowInitAnimation;
	}

	/**
	 * @return the gap (in pixels) between two consecutive cards
	 */
	public float getCardGap() {
		return mCardGap;
	}

	/**
	 * Set the gap (in pixels) between two consecutive cards
	 */
	public void setCardGap(float mCardGap) {
		this.mCardGap = mCardGap;
	}

	/**
	 * @return gap between the two consecutive cards when collapsed to the
	 *         bottom of the screen
	 */
	public float getCardGapBottom() {
		return mCardGapBottom;
	}

	public void setCardGapBottom(float mCardGapBottom) {
		this.mCardGapBottom = mCardGapBottom;
	}

	/**
	 * @return true if a card is selected, false otherwise
	 */
	public boolean isCardSelected() {
		return mAdapter.isCardSelected();
	}

	/**
	 * Removes the adapter that was previously set using
	 * {@link #setAdapter(CardStackAdapter)}
	 */
	public void removeAdapter() {
		if (getChildCount() > 0)
			removeAllViews();
		mAdapter = null;
		mOnCardSelectedListener = null;
	}

	/**
	 * Animates the cards to their initial position in the layout.
	 */
	public void restoreCards() {
		mAdapter.resetCards();
	}

	/**
	 * Intimates the implementing class about the selection of a card
	 */
	public interface OnCardSelected {
		void onCardSelected(View v, int position);
	}

}