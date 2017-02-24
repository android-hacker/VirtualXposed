package com.lody.virtual.server.notification;

import android.content.Context;
import android.os.Build;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.lody.virtual.helper.utils.OSUtils;

/**
 * Created by 247321453 on 2016/7/17.
 * notification's width
 */

/* package */ class WidthCompat {
    private final static String TAG = WidthCompat.class.getSimpleName();
    private volatile int mWidth = 0;

    public int getNotificationWidth(Context context, int width, int height, int padding) {
        if (mWidth > 0) {
            return mWidth;
        }
        int w = getDefaultWidth(width, padding);
        if (OSUtils.getInstance().isEmui()) {
            // huawei's emui
            w = getEMUINotificationWidth(context, width, height);
        } else if (OSUtils.getInstance().isMiui()) {
            if (Build.VERSION.SDK_INT >= 21) {
                padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f,
                        context.getResources().getDisplayMetrics()));
                w = getMIUINotificationWidth(context, width - padding * 2, height);
            } else {
                padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f,
                        context.getResources().getDisplayMetrics()));
                w = getMIUINotificationWidth(context, width - padding * 2, height);
            }
        }
        mWidth = w;
        return w;
    }

    private int getDefaultWidth(int width, int padding) {
        if (Build.VERSION.SDK_INT >= 21)
            return width - padding * 2;
        return width;
    }


    private int getMIUINotificationWidth(Context context, int width, int height) {
        // status_bar_notification_row
        // adaptive
        // content
        try {
            Context systemUi = context.createPackageContext(NotificationCompat.SYSTEM_UI_PKG,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            int layoutId = getSystemId(systemUi, "status_bar_notification_row", "layout");
            // status_bar_notification_row
            if (layoutId != 0) {
                ViewGroup viewGroup = createViewGroup(systemUi, layoutId);

                int lid = getSystemId(systemUi, "adaptive", "id");
                if (lid == 0) {
                    lid = getSystemId(systemUi, "content", "id");
                } else {
                    // miui5的子view不存在的空指针
                    View child = viewGroup.findViewById(lid);
                    if (child != null && child instanceof ViewGroup) {
                        ((ViewGroup) child).addView(new View(systemUi));
                    }
                }
                layout(viewGroup, width, height);
                if (lid != 0) {
                    View child = viewGroup.findViewById(lid);
                    if (child != null) {
                        return width - child.getLeft() - child.getPaddingLeft() - child.getPaddingRight();
                    }
                } else {
                    int count = viewGroup.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View child = viewGroup.getChildAt(i);
                        if (FrameLayout.class.isInstance(child) || "LatestItemView".equals(child.getClass().getName())
                                || "SizeAdaptiveLayout".equals(child.getClass().getName())) {
                            return width - child.getLeft() - child.getPaddingLeft() - child.getPaddingRight();// (LinearLayout)child;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return width;
    }

    /**
     * emui 3.0
     */
    private int getEMUINotificationWidth(Context context, int width, int height) {
        try {
            Context systemUi = context.createPackageContext(NotificationCompat.SYSTEM_UI_PKG,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            int layoutId = getSystemId(systemUi, "time_axis", "layout");
            if (layoutId != 0) {
                ViewGroup viewGroup = createViewGroup(systemUi, layoutId);
                layout(viewGroup, width, height);
                int lid = getSystemId(systemUi, "content_view_group", "id");
                if (lid != 0) {
                    View child = viewGroup.findViewById(lid);
                    return width - child.getLeft() - child.getPaddingLeft() - child.getPaddingRight();
                } else {
                    int count = viewGroup.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View child = viewGroup.getChildAt(i);
                        if (LinearLayout.class.isInstance(child)) {
                            // (LinearLayout)child;
                            return width - child.getLeft() - child.getPaddingLeft() - child.getPaddingRight();
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
        return width;
    }

    private int getSystemId(Context systemUi, String name, String type) {
        return systemUi.getResources().getIdentifier(name, type, NotificationCompat.SYSTEM_UI_PKG);
    }

    private ViewGroup createViewGroup(Context context, int layoutId) {
        try {
            return (ViewGroup) LayoutInflater.from(context).inflate(layoutId, null);
        } catch (Throwable e) {
            // ignore
        }
        return new FrameLayout(context);
    }

    private void layout(View view, int width, int height) {
        view.layout(0, 0, width, height);
        view.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST));
        view.layout(0, 0, width, height);
    }
}
