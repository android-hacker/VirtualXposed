package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.PendingIntent;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class NotificationPendIntent {
    View my;
    View view2;
    Map<Integer, PendingIntent> clickIntents;

    public NotificationPendIntent(View my, View view2, Map<Integer, PendingIntent> clickIntents) {
        this.my = my;
        this.view2 = view2;
        this.clickIntents = clickIntents;
    }

    public void set(RemoteViews remoteViews) {
        if (clickIntents != null) {
            //view2+clickIntents=>区域和事件
            Iterator<Map.Entry<Integer, PendingIntent>> set = clickIntents.entrySet().iterator();
            List<RectInfo> list = new ArrayList<>();
            //id转为为区域
            while (set.hasNext()) {
                Map.Entry<Integer, PendingIntent> e = set.next();
                View view = view2.findViewById(e.getKey());
                if (view != null) {
                    Rect rect = new Rect();
                    view.getBoundsOnScreen(rect);
                    list.add(new RectInfo(rect, e.getValue()));
                }
            }
            if (my instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) my;
                int count = viewGroup.getChildCount();
                for (int i = 0; i < count; i++) {
                    View v = viewGroup.getChildAt(i);
                    if (v instanceof ViewGroup) {
                        //linearlayout
                        ViewGroup v2 = (ViewGroup) v;
                        int c = v2.getChildCount();
                        for (int j = 0; j < c; j++) {
                            View _v = v2.getChildAt(j);
                            if (_v instanceof TextView) {
                                //textview
                                Rect rect = new Rect();
                                _v.getBoundsOnScreen(rect);
                                PendingIntent pendingIntent = findIntent(rect, list);
                                if (pendingIntent != null) {
                                    remoteViews.setOnClickPendingIntent(_v.getId(), pendingIntent);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private PendingIntent findIntent(Rect rect, List<RectInfo> list) {
        //rect在哪一个占面积最大的点击事件上面,则设置事件
        int maxArea = 0;
        PendingIntent maxIntent = null;
        for (RectInfo rectInfo : list) {
            int size = getOverlapArea(rect, rectInfo.rect);
            if (size > maxArea) {
                maxArea = size;
                maxIntent = rectInfo.mPendingIntent;
            }
        }
        return maxIntent;
    }

    //是否相交
    private boolean isOverlap(Rect rc1, Rect rc2) {
        if (rc1.left + rc1.width() > rc2.left &&
                rc2.left + rc2.width() > rc1.left &&
                rc1.top + rc1.height() > rc2.top &&
                rc2.top + rc2.height() > rc1.top
                )
            return true;
        else
            return false;
    }

    private int getOverlapArea(Rect rect1, Rect rect2) {

        if (!isOverlap(rect1, rect2)) {
            //不重叠
            return 0;
        }
        //2个区域重叠的面积
        Rect rect = new Rect();
        rect.left = Math.max(rect1.left, rect2.left);
        rect.top = Math.max(rect1.top, rect2.top);
        rect.right = Math.min(rect1.right, rect2.right);
        rect.bottom = Math.min(rect1.bottom, rect2.bottom);
        if (rect.left < rect.bottom && rect.right < rect.bottom) {
            return (rect.right - rect.left) * (rect.bottom - rect.top);
        }
        return 0;
    }

    class RectInfo {
        public RectInfo(Rect rect, PendingIntent pendingIntent) {
            this.rect = rect;
            mPendingIntent = pendingIntent;
        }

        Rect rect;
        PendingIntent mPendingIntent;
    }
}
