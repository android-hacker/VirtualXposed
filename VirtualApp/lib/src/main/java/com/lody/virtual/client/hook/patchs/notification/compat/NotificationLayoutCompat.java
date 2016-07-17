package com.lody.virtual.client.hook.patchs.notification.compat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * Created by 247321453 on 2016/7/17.
 */
public class NotificationLayoutCompat {
    /**
     * emui 3.0
     * @param context
     * @param width
     * @param height
     * @return
     */
    public int getEmuiNotificationWidth(Context context, int width, int height) {
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
        return 0;
    }
}
