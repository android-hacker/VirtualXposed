package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.lody.virtual.helper.utils.OSUtils;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.XLog;

/**
 * Created by 247321453 on 2016/7/17.
 * contentview为空的情况处理
 */
class RemoteViewsCompat {
    Context context;
    RemoteViews mRemoteViews;
    boolean mBig;
    int iconId;
    int mColor;
    float mSize;
    Notification mNotification;
    boolean mDateTime;
    int paddingRight = -1;

    public RemoteViewsCompat(Context context, NotificationActionCompat notificationActionCompat, Notification notification) {
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
            Notification my = clone(context, notification);
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

    public static Notification clone(Context context, Notification notification) {
        //插件的icon，绘制完成再替换成自己的
        Notification.Builder builder;
        boolean error = false;
        Notification notification1 = null;
        try {
            notification1 = new Notification();
            Reflect.on(notification).call("cloneInto", notification1, true);
        } catch (Exception e) {
            XLog.w("kk", "clone fail " + notification);
            notification1 = null;
        }
        if (notification1 == null) {
            try {
                builder = Reflect.on(Notification.Builder.class).create(context, notification).get();
            } catch (Exception e) {
                builder = new Notification.Builder(context);
                try {
                    Reflect.on(builder).call("restoreFromNotification", notification);
                } catch (Exception e1) {
                    error = true;
                }
            }
            if (error) {
                XLog.w("kk", "error clone notification:" + notification);
                if (Build.VERSION.SDK_INT < 23) {
                    builder.setSmallIcon(context.getApplicationInfo().icon);
                }
                if (Build.VERSION.SDK_INT >= 21) {
                    builder.setCategory(notification.category);
                    builder.setColor(notification.color);
                }
                if (Build.VERSION.SDK_INT >= 20) {
                    builder.setGroup(notification.getGroup());
                    builder.setGroupSummary(notification.isGroupSummary());
                    builder.setPriority(notification.priority);
                    builder.setSortKey(notification.getSortKey());
                }
                if (notification.sound != null) {
                    if (notification.defaults == 0) {
                        builder.setDefaults(Notification.DEFAULT_SOUND);//notification.defaults);
                    } else {
                        builder.setDefaults(Notification.DEFAULT_ALL);
                    }
                }
                builder.setLights(notification.ledARGB, notification.ledOnMS, notification.ledOffMS);
                builder.setNumber(notification.number);
                builder.setTicker(notification.tickerText);
                //intent
                builder.setContentIntent(notification.contentIntent);
                builder.setDeleteIntent(notification.deleteIntent);
                builder.setFullScreenIntent(notification.fullScreenIntent,
                        (notification.flags & Notification.FLAG_HIGH_PRIORITY) != 0);
            }
            NotificationActionCompat actionCompat = new NotificationActionCompat();
            actionCompat.builderNotificationIcon(context, notification, builder);
            if (Build.VERSION.SDK_INT >= 16) {
                notification1 = builder.build();
            } else {
                notification1 = builder.getNotification();
            }
            notification1.flags = notification.flags;
        }
        return notification1;
    }
}
