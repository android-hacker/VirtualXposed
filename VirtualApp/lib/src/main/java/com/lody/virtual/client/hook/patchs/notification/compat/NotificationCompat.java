package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.lody.virtual.helper.utils.Reflect;

/**
 * Created by 247321453 on 2016/7/17.
 * contentview为空的情况处理
 */
public class NotificationCompat {
    Context context;
    RemoteViews mRemoteViews;
    boolean mBig;
    int iconId;
    Notification mNotification;
    public NotificationCompat(Context context, NotificationActionCompat notificationActionCompat, Notification notification) {
        this.context = context;
        this.iconId = notification.icon;
        this.mNotification = deal(notification, true);
        notificationActionCompat.builderNotificationIcon(mNotification, android.R.drawable.sym_def_app_icon, context.getResources());
    }

    public Notification getNotification() {
        return mNotification;
    }

    public int getIconId() {
        return iconId;
    }

    private Notification deal(Notification notification, boolean first) {
        if (notification.contentView != null) {
            mRemoteViews = notification.contentView;
        } else {
            if (Build.VERSION.SDK_INT >= 16) {
                if (notification.bigContentView != null) {
                    mBig = true;
                    mRemoteViews = notification.bigContentView;
                }
            }
            if (mRemoteViews == null && Build.VERSION.SDK_INT >= 21) {
                Log.i("kk", "notification.publicVersion=" + notification.publicVersion);
                if (notification.publicVersion != null) {
                    if (notification.publicVersion.contentView != null) {
                        mRemoteViews = notification.publicVersion.contentView;
                    } else if (notification.publicVersion.bigContentView != null) {
                        mBig = true;
                        mRemoteViews = notification.publicVersion.bigContentView;
                    }
                }
            }
        }
        if (first && mRemoteViews == null) {
            Notification.Builder builder;
            try {
                builder = Reflect.on(Notification.Builder.class).create(context, notification).get();
            } catch (Exception e) {
                builder = new Notification.Builder(context);
                Reflect.on(builder).call("restoreFromNotification", notification);
            }
            builder.setWhen(System.currentTimeMillis());
            Notification my;
            if (Build.VERSION.SDK_INT >= 16) {
                my = builder.build();
            } else {
                my = builder.getNotification();
            }
            return deal(my, false);
            //自己去读取信息绘制
        } else {
            return notification;
        }
    }

    public RemoteViews getRemoteViews() {
        return mRemoteViews;
    }

    public boolean isBigRemoteViews() {
        return mBig;
    }
}
