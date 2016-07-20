package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.lody.virtual.helper.utils.OSUtils;
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
    int mColor;
    float mSize;
    Notification mNotification;
    boolean mDateTime;
    int paddingRight = -1;

    public NotificationCompat(Context context, NotificationActionCompat notificationActionCompat, Notification notification) {
        this.context = context;
        this.iconId = notification.icon;
        this.mNotification = deal(notification, true);
        notificationActionCompat.builderNotificationIcon(mNotification, android.R.drawable.sym_def_app_icon, context.getResources());
        findText();
    }

    public boolean hasDateTime() {
        return mDateTime;
    }

    private void findText() {
        RemoteViews remoteViews = getRemoteViews();
        if (remoteViews == null) return;
        View view = remoteViews.apply(context, null);
        View v = view.findViewById(com.android.internal.R.id.time);
        if (v == null) {
            v = view.findViewById(com.android.internal.R.id.info);
        } else {
            mDateTime = true;
        }
        if (v != null && v instanceof TextView) {
            TextView tv = (TextView) v;
            mColor = tv.getCurrentTextColor();
            mSize = tv.getTextSize();
            paddingRight = tv.getPaddingRight();
        } else {
            TextView tv = findTextView(view);
            if (tv != null) {
                mColor = tv.getCurrentTextColor();
                mSize = tv.getTextSize();
                paddingRight = 0;
            }
        }
        //
        if (OSUtils.isMIUI()) {
            mDateTime = true;
            paddingRight = 0;
        }
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public int getColor() {
        return mColor;
    }

    public float getSize() {
        return mSize;
    }

    private TextView findTextView(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            int count = viewGroup.getChildCount();
            for (int i = count - 1; i >= 0; i--) {
                View v = viewGroup.getChildAt(i);
                if (v instanceof TextView) {
                    return (TextView) v;
                } else if (v instanceof ViewGroup) {
                    TextView tv = findTextView(v);
                    if (tv != null) {
                        return tv;
                    }
                }
            }
        }
        return null;
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
