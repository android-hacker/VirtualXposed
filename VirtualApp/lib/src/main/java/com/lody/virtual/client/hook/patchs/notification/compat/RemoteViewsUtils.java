package com.lody.virtual.client.hook.patchs.notification.compat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.Gravity;
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
    private int notification_min_height, notification_max_height, notification_mid_height;
    private int notification_panel_width;
    private int notification_side_padding;
    private int notification_padding;
    private final WidthCompat mWidthCompat;
    private static RemoteViewsUtils sRemoteViewsUtils;

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

    private RemoteViewsUtils() {
        mWidthCompat = new WidthCompat();
    }

    public Bitmap createBitmap(final Context context, RemoteViews remoteViews, boolean isBig, boolean systemId) {
        View mCache = createView(context, remoteViews, isBig, systemId);
        mCache.setDrawingCacheEnabled(true);
        mCache.buildDrawingCache();
        mCache.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        return mCache.getDrawingCache();
    }

    public View createView(final Context context, RemoteViews remoteViews, boolean isBig, boolean systemId) {
        if (remoteViews == null) return null;
        init(VirtualCore.getCore().getContext());
        //TODO 需要适配
        int height = isBig ? notification_max_height : notification_min_height;
        int width = mWidthCompat.getNotificationWidth(context, notification_panel_width, height, notification_side_padding);
        ViewGroup frameLayout = new FrameLayout(context);
        View view1 = remoteViews.apply(context, frameLayout);
        View mCache;
        FrameLayout.LayoutParams params;
        if (systemId) {
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        params.gravity = Gravity.CENTER_VERTICAL;
        mCache = frameLayout;
        frameLayout.addView(view1, params);
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (!systemId) {
//                mCache = view1;
//            } else {
//                mCache = frameLayout;
//                frameLayout.addView(view1, params);
//            }
//        } else {
//            mCache = frameLayout;
//            frameLayout.addView(view1, params);
//        }
        if (view1 instanceof ViewGroup) {
            fixTextView((ViewGroup) view1);
        }
        int mode;
        //TODO 各种适配
        if (systemId) {
            mode = View.MeasureSpec.EXACTLY;
        } else {
            mode = View.MeasureSpec.EXACTLY;
        }
        mCache.layout(0, 0, width, height);
        mCache.measure(View.MeasureSpec.makeMeasureSpec(width, mode), View.MeasureSpec.makeMeasureSpec(height, mode));
        mCache.layout(0, 0, width, height);
        VLog.i("kk", "max=%d/%d, szie=%d/%d", width, height, mCache.getMeasuredWidth(), mCache.getMeasuredHeight());
        //打印action
//        logActions(remoteViews, view1);
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
                notification_side_padding = getDimem(context, systemUi, "notification_side_padding", R.dimen.notification_side_padding);
            }
            notification_panel_width = getDimem(context, systemUi, "notification_panel_width", R.dimen.notification_panel_width);
            if (notification_panel_width <= 0) {
                notification_panel_width = context.getResources().getDisplayMetrics().widthPixels;
            }
            notification_min_height = getDimem(context, systemUi, "notification_min_height", R.dimen.notification_min_height);
//            getDimem(context, systemUi, "notification_row_min_height", 0);
//            if (notification_min_height == 0) {
//                notification_min_height =
//            }
            notification_max_height = getDimem(context, systemUi, "notification_max_height", R.dimen.notification_max_height);
            notification_mid_height = getDimem(context, systemUi, "notification_mid_height", R.dimen.notification_mid_height);
            notification_padding = getDimem(context, systemUi, "notification_padding", R.dimen.notification_padding);
            //notification_collapse_second_card_padding
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
//        VLog.w(TAG, "use my dimen:" + name);
        return defId == 0 ? 0 : Math.round(context.getResources().getDimension(defId));
    }

}
