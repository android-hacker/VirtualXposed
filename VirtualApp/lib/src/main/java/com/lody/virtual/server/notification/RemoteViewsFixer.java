package com.lody.virtual.server.notification;

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
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;

import java.util.ArrayList;
import java.util.HashMap;


/* package */ class RemoteViewsFixer {
    private static final String TAG = NotificationCompat.TAG;
    private final WidthCompat mWidthCompat;
    private int notification_min_height, notification_max_height, notification_mid_height;
    private int notification_panel_width;
    private int notification_side_padding;
    private int notification_padding;

    private final HashMap<String, Bitmap> mImages = new HashMap<>();
    private NotificationCompat mNotificationCompat;

    RemoteViewsFixer(NotificationCompat notificationCompat) {
        mWidthCompat = new WidthCompat();
        mNotificationCompat = notificationCompat;
    }

    View toView(final Context context, RemoteViews remoteViews, boolean isBig, boolean systemId) {
        View mCache = null;
        try {
            mCache = createView(context, remoteViews, isBig, systemId);
        } catch (Throwable throwable) {
            VLog.w(TAG, "toView 1", throwable);
            try {
                mCache = LayoutInflater.from(context).inflate(remoteViews.getLayoutId(), null);
            } catch (Throwable e) {
                VLog.w(TAG, "toView 2", e);
            }
        }
        return mCache;
    }

    Bitmap createBitmap(View mCache) {
        if (mCache == null) {
            return null;
        }
        mCache.setDrawingCacheEnabled(true);
        mCache.buildDrawingCache();
        return mCache.getDrawingCache();
    }

    private View apply(Context context, RemoteViews remoteViews) {
        View view = null;
        try {
            view = LayoutInflater.from(context).inflate(remoteViews.getLayoutId(), null, false);
            try {
                Reflect.on(view).call("setTagInternal", Reflect.on("com.android.internal.R$id").get("widget_frame"), remoteViews.getLayoutId());
            } catch (Exception e2) {
                VLog.w(TAG, "setTagInternal", e2);
            }
        } catch (Exception e) {
            VLog.w(TAG, "inflate", e);
        }
        if (view != null) {
            ArrayList<Object> mActions = Reflect.on(remoteViews).get("mActions");
            if (mActions != null) {
                VLog.d(TAG, "apply actions:"+mActions.size());
                for (Object action : mActions) {
                    try {
                        Reflect.on(action).call("apply", view, null, null);
                    } catch (Exception e) {
                        VLog.w(TAG, "apply action", e);
                    }
                }
            }
        } else {
            VLog.e(TAG, "create views");
        }
        return view;
    }

    private View createView(final Context context, RemoteViews remoteViews, boolean isBig, boolean systemId) {
        if (remoteViews == null)
            return null;
        Context base = mNotificationCompat.getHostContext();
        init(base);
        VLog.v(TAG, "createView:big=" + isBig + ",system=" + systemId);

        int height = isBig ? notification_max_height : notification_min_height;
        int width = mWidthCompat.getNotificationWidth(base, notification_panel_width, height,
                notification_side_padding);
        VLog.v(TAG, "createView:getNotificationWidth=" + width);
        ViewGroup frameLayout = new FrameLayout(context);
        VLog.v(TAG, "createView:apply");

        View view1 = apply(context, remoteViews);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        frameLayout.addView(view1, params);
        if (view1 instanceof ViewGroup) {
            VLog.v(TAG, "createView:fixTextView");
            fixTextView((ViewGroup) view1);
        }
        int mode;
        //TODO need adaptation
        if (systemId) {
            mode = View.MeasureSpec.EXACTLY;
        } else {
            if (isBig) {
                mode = View.MeasureSpec.AT_MOST;
            } else {
                mode = View.MeasureSpec.EXACTLY;
            }
        }
        VLog.v(TAG, "createView:layout");
        View mCache = frameLayout;
        mCache.layout(0, 0, width, height);
        mCache.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(height, mode));
        mCache.layout(0, 0, width, mCache.getMeasuredHeight());
        VLog.v(TAG, "notification:systemId=" + systemId + ",max=%d/%d, szie=%d/%d", width, height,
                mCache.getMeasuredWidth(), mCache.getMeasuredHeight());
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

    public RemoteViews makeRemoteViews(String key, Context pluginContext, RemoteViews contentView, boolean isBig, boolean click) {
        if (contentView == null) {
            return null;
        }
        final boolean systemId = false;
        final PendIntentCompat pendIntentCompat = new PendIntentCompat(contentView);
        final int layoutId;
        if (!click || pendIntentCompat.findPendIntents() <= 0) {
            layoutId = R.layout.custom_notification_lite;
        } else {
            layoutId = R.layout.custom_notification;
        }
        VLog.v(TAG, "createviews id = " + layoutId);
        //make a remoteViews
        RemoteViews remoteViews = new RemoteViews(mNotificationCompat.getHostContext().getPackageName(), layoutId);
        VLog.v(TAG, "remoteViews to view");
        View cache = toView(pluginContext, contentView, isBig, systemId);
        // remoteViews to bitmap
        VLog.v(TAG, "start createBitmap");
        final Bitmap bmp = createBitmap(cache);
        if (bmp == null) {
            VLog.e(TAG, "bmp is null,contentView=" + contentView);
            // return null; //ignore notification
        } else {
            VLog.v(TAG, "bmp w=" + bmp.getWidth() + ",h=" + bmp.getHeight());
        }
        Bitmap old;
        synchronized (mImages) {
            old = mImages.get(key);
        }
        if (old != null && !old.isRecycled()) {
            VLog.v(TAG, "recycle " + key);
            old.recycle();
        }
        remoteViews.setImageViewBitmap(R.id.im_main, bmp);
        VLog.v(TAG, "createview " + key);
        synchronized (mImages) {
            mImages.put(key, bmp);
        }
        //notification's click
        if (click) {
            if (layoutId == R.layout.custom_notification) {
                VLog.v(TAG, "start setPendIntent");
                try {
                    pendIntentCompat.setPendIntent(remoteViews,
                            toView(mNotificationCompat.getHostContext(), remoteViews, isBig, systemId),
                            cache);
                } catch (Exception e) {
                    VLog.e(TAG, "setPendIntent error", e);
                }
            }
        }
        return remoteViews;
    }

    private boolean init = false;

    private void init(Context context) {
        if (init) return;
        init = true;
        if (notification_panel_width == 0) {
            Context systemUi = null;
            try {
                systemUi = context.createPackageContext(NotificationCompat.SYSTEM_UI_PKG, Context.CONTEXT_IGNORE_SECURITY);
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
            int id = sysContext.getResources().getIdentifier(name, "dimen", NotificationCompat.SYSTEM_UI_PKG);
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
