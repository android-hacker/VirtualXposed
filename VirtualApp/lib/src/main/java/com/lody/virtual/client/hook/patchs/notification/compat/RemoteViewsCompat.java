package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.widget.RemoteViews;

import com.lody.virtual.helper.utils.VLog;

/**
 * Created by 247321453 on 2016/7/17.
 * ContentView 为空的情况处理
 * 处理系统样式的通知栏的时间显示
 */
class RemoteViewsCompat {
    private Context context;
    private RemoteViews mRemoteViews;
    private RemoteViews mBigRemoteViews;
    private RemoteViews mHeadsUpContentView;
    private Notification mNotification;
    private int paddingRight = -1;

    public RemoteViewsCompat(Context context, Notification notification) {
        this.context = context;
        this.mNotification = checkNotNull(notification, true);
    }

    public Notification getNotification() {
        return mNotification;
    }

    private Notification checkNotNull(Notification notification, boolean first) {
        if (notification.contentView != null) {
            mRemoteViews = notification.contentView;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            if (notification.bigContentView != null) {
                mBigRemoteViews = notification.bigContentView;
            }
        }
        if (Build.VERSION.SDK_INT >= 21) {
            mHeadsUpContentView = notification.headsUpContentView;
            if (notification.publicVersion != null) {
                if (mRemoteViews == null && notification.publicVersion.contentView != null) {
                    mRemoteViews = notification.publicVersion.contentView;
                }
                if (mBigRemoteViews == null && notification.publicVersion.bigContentView != null) {
                    mBigRemoteViews = notification.publicVersion.bigContentView;
                }
                if (mHeadsUpContentView == null) {
                    mHeadsUpContentView = notification.publicVersion.headsUpContentView;
                }
            }
        }
        if (first && (mRemoteViews == null && mBigRemoteViews == null)) {
            //都为空
            Notification my = NotificaitionUtils.clone(context, notification);
            return checkNotNull(my, false);
            //自己去读取信息绘制
        } else {
            return notification;
        }
    }

    public RemoteViews getRemoteViews() {
        if (mRemoteViews == null) {
            VLog.w("notification", "contentView == null");
        }
        return mRemoteViews;
    }

    public RemoteViews getBigRemoteViews() {
        return mBigRemoteViews;
    }

    public RemoteViews getHeadsUpContentView() {
        return mHeadsUpContentView;
    }
}
