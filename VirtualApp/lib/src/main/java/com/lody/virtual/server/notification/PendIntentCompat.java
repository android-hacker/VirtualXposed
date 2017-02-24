package com.lody.virtual.server.notification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;

import android.app.PendingIntent;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;

import static com.lody.virtual.server.notification.NotificationCompat.TAG;

/***
 * Remoteviews's PendIntent
 *
 * @author 247321453
 */
class PendIntentCompat {
    private RemoteViews mRemoteViews;
    private Map<Integer, PendingIntent> clickIntents;

    PendIntentCompat(RemoteViews mRemoteViews) {
        this.mRemoteViews = mRemoteViews;
    }

    public int findPendIntents() {
        if (clickIntents == null) {
            clickIntents = getClickIntents(mRemoteViews);
        }
        return clickIntents.size();
    }

    /**
     *
     * @param remoteViews notification's old remoteViews
     * @param remoteview notification's old remoteViews view
     * @param oldRemoteView notification's new remoteViews view
     */
    public void setPendIntent(RemoteViews remoteViews, View remoteview, View oldRemoteView) {
        if (findPendIntents() > 0) {
            Iterator<Map.Entry<Integer, PendingIntent>> set = clickIntents.entrySet().iterator();
            List<RectInfo> list = new ArrayList<>();
            int index = 0;
            VLog.v(TAG, "start find intent");
            while (set.hasNext()) {
                Map.Entry<Integer, PendingIntent> e = set.next();
                View view = oldRemoteView.findViewById(e.getKey());
                if (view != null) {
                    Rect rect = getRect(view);
                    list.add(new RectInfo(rect, e.getValue(), index));
                    index++;
                }
            }
            VLog.v(TAG, "find:" + list);
            if (remoteview instanceof ViewGroup) {
                setIntentByViewGroup(remoteViews, (ViewGroup) remoteview, list);
            }
        }
    }

    private Rect getRect(View view) {
        Rect rect = new Rect();
        rect.top = view.getTop();
        rect.left = view.getLeft();
        rect.right = view.getRight();
        rect.bottom = view.getBottom();

        ViewParent viewParent = view.getParent();
        if (viewParent != null) {
            if (viewParent instanceof ViewGroup) {
                Rect prect = getRect((ViewGroup) viewParent);
                rect.top += prect.top;
                rect.left += prect.left;
                rect.right += prect.left;
                rect.bottom += prect.top;
            }
        }
        return rect;
    }

    private void setIntentByViewGroup(RemoteViews remoteViews, ViewGroup viewGroup, List<RectInfo> list) {
        int count = viewGroup.getChildCount();
        Rect p = new Rect();
        viewGroup.getHitRect(p);
        for (int i = 0; i < count; i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof ViewGroup) {
                // linearlayout
                setIntentByViewGroup(remoteViews, (ViewGroup) v, list);
            } else if (v instanceof TextView || v instanceof ImageView) {
                // textview
                Rect rect = getRect(v);
                RectInfo next = findIntent(rect, list);
                if (next != null) {
//					VLog.d(TAG, next.rect+":setPendIntent:"+i);
//                    remoteViews.setImageViewBitmap(v.getId(), next.testBg);
                    remoteViews.setOnClickPendingIntent(v.getId(), next.mPendingIntent);
                }
            }
        }
    }

    private RectInfo findIntent(Rect rect, List<RectInfo> list) {
        int maxArea = 0;
        RectInfo next = null;
        for (RectInfo rectInfo : list) {
            int size = getOverlapArea(rect, rectInfo.rect);
            if (size > maxArea) {
                if (size == 0) {
                    Log.w("PendingIntentCompat", "find two:" + rectInfo.rect);
                }
                maxArea = size;
                next = rectInfo;
            }
        }
        return next;
    }

    private int getOverlapArea(Rect rect1, Rect rect2) {
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

    private Map<Integer, PendingIntent> getClickIntents(RemoteViews remoteViews) {
        Map<Integer, PendingIntent> map = new HashMap<>();
        if (remoteViews == null)
            return map;
        Object mActionsObj = null;
        try {
            mActionsObj = Reflect.on(remoteViews).get("mActions");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mActionsObj == null) {
            return map;
        }
        if (mActionsObj instanceof Collection) {
            Collection mActions = (Collection) mActionsObj;
            for (Object one : mActions) {
                if (one != null) {
                    String action;
                    try {
                        action = Reflect.on(one).call("getActionName").get();
                    } catch (Exception e) {
                        action = one.getClass().getSimpleName();
                    }
                    if ("SetOnClickPendingIntent".equalsIgnoreCase(action)) {
                        int id = Reflect.on(one).get("viewId");
                        PendingIntent intent = Reflect.on(one).get("pendingIntent");
                        map.put(id, intent);
                    }
                }
            }
        }
        return map;
    }

    class RectInfo {
        Rect rect;
        PendingIntent mPendingIntent;
        int index;

        public RectInfo(Rect rect, PendingIntent pendingIntent, int index) {
            this.rect = rect;
            mPendingIntent = pendingIntent;
            this.index = index;
        }

        @Override
        public String toString() {
            return "RectInfo{" +
                    "rect=" + rect +
                    '}';
        }
    }
}
