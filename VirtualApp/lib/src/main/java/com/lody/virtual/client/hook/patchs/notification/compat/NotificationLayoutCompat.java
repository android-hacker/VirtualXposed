package com.lody.virtual.client.hook.patchs.notification.compat;

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
 */
public class NotificationLayoutCompat {
    private final static String TAG = NotificationLayoutCompat.class.getSimpleName();
    public int getNotificationWidth(Context context, int width, int height, int padding) {
//TODO 适配各种rom
        int w = getWidth(width, padding);
        if (OSUtils.isEMUI()) {
            //华为emui
            w = getEMUINotificationWidth(context, width, height);
        } else if (OSUtils.isMIUI()) {
//            if ("4".equals(OSUtils.getMIUIVersion())) {
            if (Build.VERSION.SDK_INT >= 21) {
                padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, context.getResources().getDisplayMetrics()));
                w = getMIUINotificationWidth(context, width - padding * 2, height);
            } else {
                padding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25f, context.getResources().getDisplayMetrics()));
                w = getMIUINotificationWidth(context, width - padding * 2, height);
            }
//            } else {
//                w = getMIUINotificationWidth(context, width, height);
//            }
        }
        return w;
    }

    private int getWidth(int width, int padding) {
        if (Build.VERSION.SDK_INT >= 21)
            return width - padding * 2;
        return width;
    }

    private int getMIUINotificationWidth(Context context, int width, int height) {
        //status_bar_notification_row
        //adaptive
        //content
        try {
            Context systemUi = context.createPackageContext("com.android.systemui", Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
            int id = systemUi.getResources().getIdentifier("status_bar_notification_row", "layout", "com.android.systemui");
            //status_bar_notification_row
            if (id != 0) {
                ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(systemUi).inflate(id, null);
                int lid = systemUi.getResources().getIdentifier("adaptive", "id", "com.android.systemui");
                if (lid == 0) {
                    lid = systemUi.getResources().getIdentifier("content", "id", "com.android.systemui");
                } else {
                    View child = viewGroup.findViewById(lid);
                    if (child != null && child instanceof ViewGroup) {
                        ((ViewGroup) child).addView(new View(systemUi));
                    }
                }
                viewGroup.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST));
                viewGroup.layout(0, 0, width, height);
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
//                            child.setPadding(0,0,0,0);
                            //  child.getRight()-child.getLeft();
                            return width - child.getLeft() - child.getPaddingLeft() - child.getPaddingRight();//(LinearLayout)child;
                        }
                    }
                }
            }
        } catch (Exception e) {

        }
        return width;
    }

    /**
     * emui 3.0
     *
     * @param context
     * @param width
     * @param height
     * @return
     */
    private int getEMUINotificationWidth(Context context, int width, int height) {
        try {
            Context systemUi = context.createPackageContext("com.android.systemui", Context.CONTEXT_IGNORE_SECURITY);
            int id = systemUi.getResources().getIdentifier("time_axis", "layout", "com.android.systemui");
            if (id != 0) {
                ViewGroup viewGroup = (ViewGroup) LayoutInflater.from(systemUi).inflate(id, null);
                int lid = systemUi.getResources().getIdentifier("content_view_group", "id", "com.android.systemui");
                viewGroup.measure(View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST));
                viewGroup.layout(0, 0, width, height);
                if (lid != 0) {
                    View child = viewGroup.findViewById(lid);
                    return width - child.getLeft() - child.getPaddingLeft() - child.getPaddingRight();
                } else {
                    int count = viewGroup.getChildCount();
                    for (int i = 0; i < count; i++) {
                        View child = viewGroup.getChildAt(i);
                        if (LinearLayout.class.isInstance(child)) {
//                            child.setPadding(0,0,0,0);
                            return width - child.getLeft() - child.getPaddingLeft() - child.getPaddingRight();//(LinearLayout)child;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return width;
    }
}
