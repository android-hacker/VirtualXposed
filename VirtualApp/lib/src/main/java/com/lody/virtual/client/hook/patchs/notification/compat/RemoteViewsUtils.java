package com.lody.virtual.client.hook.patchs.notification.compat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.lody.virtual.R;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.env.Constants;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;

class RemoteViewsUtils {
	private static final String TAG = RemoteViewsUtils.class.getSimpleName();
	private static RemoteViewsUtils sRemoteViewsUtils;
	private final WidthCompat mWidthCompat;
	private int notification_min_height, notification_max_height, notification_mid_height;
	private int notification_panel_width;
	private int notification_side_padding;
	private int notification_padding;

	private RemoteViewsUtils() {
		mWidthCompat = new WidthCompat();
	}

	public static RemoteViewsUtils getInstance() {
		if (sRemoteViewsUtils == null) {
			synchronized (RemoteViewsUtils.class) {
				if (sRemoteViewsUtils == null) {
					sRemoteViewsUtils = new RemoteViewsUtils();
				}
			}
		}
		return sRemoteViewsUtils;
	}

	public Bitmap createBitmap(final Context context, RemoteViews remoteViews, boolean isBig, boolean systemId) {
		View mCache = null;
		try {
			mCache = createView(context, remoteViews, isBig, systemId);
		} catch (Throwable throwable) {
			try {
				// apply失败后,根据布局id创建view
				mCache = LayoutInflater.from(context).inflate(remoteViews.getLayoutId(), null);
			} catch (Throwable e) {

			}
		}
		if (mCache == null) {
			return null;
		}
		mCache.setDrawingCacheEnabled(true);
		mCache.buildDrawingCache();
		mCache.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
		return mCache.getDrawingCache();
	}

	public View createView(final Context context, RemoteViews remoteViews, boolean isBig, boolean systemId) {
		if (remoteViews == null)
			return null;
		Context base = VirtualCore.get().getContext();
		init(base);
		// TODO 需要适配
		int height = isBig ? notification_max_height : notification_min_height;
		int width = mWidthCompat.getNotificationWidth(base, notification_panel_width, height,
				notification_side_padding);
		ViewGroup frameLayout = new FrameLayout(context);
		View view1 = remoteViews.apply(context, frameLayout);
		View mCache;
		FrameLayout.LayoutParams params;
		if (systemId) {
			params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
		} else {
			params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		// if (systemId) {
		// Log.i("kk", "find icon");
		// View icon = view1.findViewById(com.android.internal.R.id.icon);
		// if (icon != null) {
		// icon.setVisibility(View.INVISIBLE);
		// Log.w("kk", "find icon");
		// } else {
		// Log.w("kk", "no find icon");
		// }
		// } else {
		// Log.i("kk", "no is systemid:" + remoteViews.getLayoutId());
		// }
		params.gravity = Gravity.CENTER_VERTICAL;
		mCache = frameLayout;
		frameLayout.addView(view1, params);
		// if (Build.VERSION.SDK_INT >= 23) {
		// if (!systemId) {
		// mCache = view1;
		// } else {
		// mCache = frameLayout;
		// frameLayout.addView(view1, params);
		// }
		// } else {
		// mCache = frameLayout;
		// frameLayout.addView(view1, params);
		// }
		if (view1 instanceof ViewGroup) {
			fixTextView((ViewGroup) view1);
		}
		int mode;
		// TODO 各种适配
		if (systemId) {
			mode = View.MeasureSpec.EXACTLY;
		} else {
			mode = View.MeasureSpec.EXACTLY;
		}
		mCache.layout(0, 0, width, height);
		mCache.measure(View.MeasureSpec.makeMeasureSpec(width, mode), View.MeasureSpec.makeMeasureSpec(height, mode));
		mCache.layout(0, 0, width, height);
		VLog.i(TAG, "notification:systemId=" + systemId + ",max=%d/%d, szie=%d/%d", width, height,
				mCache.getMeasuredWidth(), mCache.getMeasuredHeight());
		// 打印action
		// logActions(remoteViews, view1);
		return mCache;
	}

	private void fixTextView(ViewGroup viewGroup) {
		int count = viewGroup.getChildCount();
		for (int i = 0; i < count; i++) {
			View v = viewGroup.getChildAt(i);
			if (v instanceof TextView) {
				TextView tv = (TextView) v;
				if (isSingleLine(tv)) {
					tv.setSingleLine(false);
					tv.setMaxLines(1);
				}
			} else if (v instanceof ViewGroup) {
				fixTextView((ViewGroup) v);
			}
		}
	}

	private boolean isSingleLine(TextView textView) {
		boolean singleLine;
		try {
			singleLine = Reflect.on(textView).get("mSingleLine");
		} catch (Exception e) {
			singleLine = (textView.getInputType() & EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE) != 0;
		}
		return singleLine;
	}

	public RemoteViews createViews(Context context, Context pluginContext, RemoteViews contentView, boolean isBig) {
		if (contentView == null) {
			return null;
		}
		final boolean systemId = !NotificationUtils.isSystemLayout(contentView);
		final PendIntentCompat pendIntentCompat = new PendIntentCompat(contentView);
		// 根据点击时间选择布局(优化)
		final int layoutId;
		if (pendIntentCompat.findPendIntents() <= 0) {
			// 如果就一个点击事件，没必要用复杂view
			layoutId = R.layout.custom_notification_lite;
		} else {
			layoutId = R.layout.custom_notification;
		}
		// 代理view创建
		RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layoutId);
		// 目标显示的内容绘制成bitmap
		final Bitmap bmp = RemoteViewsUtils.getInstance().createBitmap(pluginContext, contentView, isBig, systemId);
		if (bmp == null) {
			VLog.e(TAG, "bmp is null,contentView=" + contentView);
			// 出错也显示空白的，要不改为系统布局？
			// return null;
		}
		remoteViews.setImageViewBitmap(R.id.im_main, bmp);
		// 点击事件
		if (layoutId == R.layout.custom_notification) {
			// 根据旧view的点击事件，设置区域点击事件
			pendIntentCompat.setPendIntent(remoteViews,
					RemoteViewsUtils.getInstance().createView(context, remoteViews, isBig, systemId),
					RemoteViewsUtils.getInstance().createView(pluginContext, contentView, isBig, systemId));
		}
		return remoteViews;
	}

	private void init(Context context) {
		if (notification_panel_width == 0) {
			Context systemUi = null;
			try {
				systemUi = context.createPackageContext("com.android.systemui", Context.CONTEXT_IGNORE_SECURITY);
			} catch (PackageManager.NameNotFoundException e) {
			}
			if (Build.VERSION.SDK_INT <= 19) {
				notification_side_padding = 0;
			} else {
				notification_side_padding = getDimem(context, systemUi, "notification_side_padding",
						R.dimen.notification_side_padding);
			}
			notification_panel_width = getDimem(context, systemUi, "notification_panel_width",
					R.dimen.notification_panel_width);
			if (notification_panel_width <= 0) {
				notification_panel_width = context.getResources().getDisplayMetrics().widthPixels;
			}
			notification_min_height = getDimem(context, systemUi, "notification_min_height",
					R.dimen.notification_min_height);
			// getDimem(context, systemUi, "notification_row_min_height", 0);
			// if (notification_min_height == 0) {
			// notification_min_height =
			// }
			notification_max_height = getDimem(context, systemUi, "notification_max_height",
					R.dimen.notification_max_height);
			notification_mid_height = getDimem(context, systemUi, "notification_mid_height",
					R.dimen.notification_mid_height);
			notification_padding = getDimem(context, systemUi, "notification_padding", R.dimen.notification_padding);
			// notification_collapse_second_card_padding
		}
	}

	private int getDimem(Context context, Context sysContext, String name, int defId) {
		if (sysContext != null) {
			int id = sysContext.getResources().getIdentifier(name, "dimen", Constants.SYSTEM_UI_PKG);
			if (id != 0) {
				try {
					return Math.round(sysContext.getResources().getDimension(id));
				} catch (Exception e) {

				}
			}
		}
		// VLog.w(TAG, "use my dimen:" + name);
		return defId == 0 ? 0 : Math.round(context.getResources().getDimension(defId));
	}

}
