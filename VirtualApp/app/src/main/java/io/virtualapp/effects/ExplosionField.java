package io.virtualapp.effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;

import io.virtualapp.VApp;
import io.virtualapp.abs.ui.VUiKit;

public class ExplosionField extends View {

	private static final Canvas sCanvas = new Canvas();
	private List<ExplosionAnimator> mExplosions = new ArrayList<>();
	private int[] mExpandInset = new int[2];

	public ExplosionField(Context context) {
		super(context);
		init();
	}

	public ExplosionField(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ExplosionField(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public static Bitmap createBitmapFromView(View view) {
		if (view instanceof ImageView) {
			Drawable drawable = ((ImageView) view).getDrawable();
			if (drawable != null && drawable instanceof BitmapDrawable) {
				return ((BitmapDrawable) drawable).getBitmap();
			}
		}
		view.clearFocus();
		Bitmap bitmap = createBitmapSafely(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888, 1);
		if (bitmap != null) {
			synchronized (sCanvas) {
				Canvas canvas = sCanvas;
				canvas.setBitmap(bitmap);
				view.draw(canvas);
				canvas.setBitmap(null);
			}
		}
		return bitmap;
	}

	public static Bitmap createBitmapSafely(int width, int height, Bitmap.Config config, int retryCount) {
		try {
			return Bitmap.createBitmap(width, height, config);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			if (retryCount > 0) {
				System.gc();
				return createBitmapSafely(width, height, config, retryCount - 1);
			}
			return null;
		}
	}

	public static ExplosionField attachToWindow(Activity activity) {
		ViewGroup rootView = (ViewGroup) activity.findViewById(Window.ID_ANDROID_CONTENT);
		ExplosionField explosionField = new ExplosionField(activity);
		rootView.addView(explosionField,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		return explosionField;
	}

	public static ExplosionField attachToWindow(ViewGroup rootView, Activity activity) {
		ExplosionField explosionField = new ExplosionField(activity);
		rootView.addView(explosionField,
				new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		return explosionField;
	}

	private void init() {
		Arrays.fill(mExpandInset, VUiKit.dpToPx(VApp.getApp(), 32));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (ExplosionAnimator explosion : mExplosions) {
			explosion.draw(canvas);
		}
	}

	public void expandExplosionBound(int dx, int dy) {
		mExpandInset[0] = dx;
		mExpandInset[1] = dy;
	}

	public void explode(Bitmap bitmap, Rect bound, long startDelay, long duration) {
		final ExplosionAnimator explosion = new ExplosionAnimator(this, bitmap, bound);
		explosion.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				mExplosions.remove(animation);
			}
		});
		explosion.setStartDelay(startDelay);
		explosion.setDuration(duration);
		mExplosions.add(explosion);
		explosion.start();
	}

	public void explode(final View view) {
		explode(view, null);
	}

	public void explode(final View view, OnExplodeFinishListener listener) {
		Rect r = new Rect();
		view.getGlobalVisibleRect(r);
		int[] location = new int[2];
		getLocationOnScreen(location);
		r.offset(-location[0], -location[1]);
		r.inset(-mExpandInset[0], -mExpandInset[1]);
		int startDelay = 100;
		ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f).setDuration(150);
		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			Random random = new Random();

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				view.setTranslationX((random.nextFloat() - 0.5f) * view.getWidth() * 0.05f);
				view.setTranslationY((random.nextFloat() - 0.5f) * view.getHeight() * 0.05f);
			}

		});
		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (listener != null) {
					listener.onExplodeFinish(view);
				}
			}
		});
		animator.start();
		view.animate().setDuration(150).setStartDelay(startDelay).scaleX(0f).scaleY(0f).alpha(0f).start();
		explode(createBitmapFromView(view), r, startDelay, ExplosionAnimator.DEFAULT_DURATION);
	}

	public void clear() {
		mExplosions.clear();
		invalidate();
	}

	public interface OnExplodeFinishListener {
		void onExplodeFinish(View v);
	}
}
