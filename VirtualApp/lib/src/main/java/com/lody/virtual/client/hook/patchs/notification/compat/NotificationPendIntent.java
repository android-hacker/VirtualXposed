package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.PendingIntent;
import android.graphics.Rect;
import android.util.Log;
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
                    view.getHitRect(rect);
//                    Log.i("kk", view.getId() + ",rect=" + rect + ",intent=" + e.getValue().getIntent().getParcelableExtra(ExtraConstants.EXTRA_INTENT));
                    list.add(new RectInfo(rect, e.getValue()));
                }
            }
            if (my instanceof ViewGroup) {
                setIntentByViewGroup(remoteViews, (ViewGroup) my, list);
            }
        }
    }

    private void setIntentByViewGroup(RemoteViews remoteViews, ViewGroup viewGroup, List<RectInfo> list) {
        int count = viewGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                //linearlayout
                setIntentByViewGroup(remoteViews, (ViewGroup) v, list);
            } else if (v instanceof TextView) {
                //textview
                Rect rect = new Rect();
                v.getHitRect(rect);
                rect.top += viewGroup.getTop();
                rect.bottom += viewGroup.getTop();
//                Log.d("kk", v.getId() + ",rect=" + rect);
                PendingIntent pendingIntent = findIntent(rect, list);
                if (pendingIntent != null) {
//                    Log.d("kk", v.getId() + " set click =" + pendingIntent.getIntent().getParcelableExtra(ExtraConstants.EXTRA_INTENT));
                    remoteViews.setOnClickPendingIntent(v.getId(), pendingIntent);
                } else {
//                    Log.w("kk", v.getId() + ",rect=" + rect);
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
                if (size == 0) {
                    Log.w("kk", "find two:" + rectInfo.rect);
                }
                maxArea = size;
                maxIntent = rectInfo.mPendingIntent;
            }
        }
        return maxIntent;
    }

    private int getOverlapArea(Rect rect1, Rect rect2) {
        //2个区域重叠的面积
        Rect rect = new Rect();
        rect.left = Math.max(rect1.left, rect2.left);
        rect.top = Math.max(rect1.top, rect2.top);
        rect.right = Math.min(rect1.right, rect2.right);
        rect.bottom = Math.min(rect1.bottom, rect2.bottom);
        if (rect.left < rect.right && rect.top < rect.bottom) {
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
