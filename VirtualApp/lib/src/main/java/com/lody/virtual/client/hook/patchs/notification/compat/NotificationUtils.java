package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.collection.SparseArray;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import mirror.com.android.internal.R_Hide;

/* package */ class NotificationUtils {

    private static final String TAG = NotificationUtils.class.getSimpleName();

    private static SparseArray<String> sSystemLayoutResIds = new SparseArray<>(10);

    static {
        loadSystemLayoutRes();
    }

    private static void loadSystemLayoutRes() {
        Field[] fields = R_Hide.TYPE.getFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())) {
                try {
                    int id = field.getInt(null);
                    sSystemLayoutResIds.put(id, field.getName());
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static boolean isSystemLayout(RemoteViews remoteViews) {
        return remoteViews != null
                && sSystemLayoutResIds.get(remoteViews.getLayoutId()) == null;
    }


    public static Notification clone(Context context, Notification notification) {
        Notification cloneNotification;
        Notification.Builder builder;
        try {
            builder = Reflect.on(Notification.Builder.class).create(context, notification).get();
        } catch (Exception e) {
            builder = createBuilder(context, notification);
        }
        fixNotificationIcon(context, notification, builder);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            cloneNotification = builder.build();
        } else {
            //noinspection deprecation
            cloneNotification = builder.getNotification();
        }
        cloneNotification.flags = notification.flags;
        //noinspection deprecation
        cloneNotification.icon = notification.icon;

        if (cloneNotification.contentIntent == null) {
            cloneNotification.contentIntent = notification.contentIntent;
        }
        if (cloneNotification.deleteIntent == null) {
            cloneNotification.deleteIntent = notification.deleteIntent;
        }
        if (cloneNotification.fullScreenIntent == null) {
            cloneNotification.fullScreenIntent = notification.fullScreenIntent;
        }
        if (cloneNotification.contentView == null) {
            cloneNotification.contentView = notification.contentView;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (cloneNotification.bigContentView == null) {
                cloneNotification.bigContentView = notification.bigContentView;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cloneNotification.publicVersion = notification.publicVersion;
            if (cloneNotification.headsUpContentView == null) {
                cloneNotification.headsUpContentView = notification.headsUpContentView;
            }
        }
        // }
        return cloneNotification;
    }

    private static Notification.Builder createBuilder(Context context, Notification notification) {
        Notification.Builder builder = new Notification.Builder(context);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            builder.setSmallIcon(context.getApplicationInfo().icon);
            //noinspection deprecation
            builder.setLargeIcon(notification.largeIcon);
        } else {
            builder.setSmallIcon(notification.getSmallIcon());
            builder.setLargeIcon(notification.getLargeIcon());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setCategory(notification.category);
            builder.setColor(notification.color);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            builder.setGroup(notification.getGroup());
//			builder.setGroupSummary(notification.isGroupSummary());
            builder.setPriority(notification.priority);
            builder.setSortKey(notification.getSortKey());
        }
        if (notification.sound != null) {
            if (notification.defaults == 0) {
                builder.setDefaults(Notification.DEFAULT_SOUND);// notification.defaults);
            } else {
                builder.setDefaults(Notification.DEFAULT_ALL);
            }
        }
        builder.setLights(notification.ledARGB, notification.ledOnMS, notification.ledOffMS);
        builder.setNumber(notification.number);
        builder.setTicker(notification.tickerText);
        // intent
        builder.setContentIntent(notification.contentIntent);
        builder.setDeleteIntent(notification.deleteIntent);
        //noinspection deprecation
        builder.setFullScreenIntent(notification.fullScreenIntent,
                (notification.flags & Notification.FLAG_HIGH_PRIORITY) != 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            builder.setContentTitle(notification.extras.getString(Notification.EXTRA_TITLE));
            builder.setContentText(notification.extras.getString(Notification.EXTRA_TEXT));
            builder.setSubText(notification.extras.getString(Notification.EXTRA_SUB_TEXT));
        }
        return builder;
    }

    static void fixNotificationIcon(Context context, Notification notification, String packageName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //noinspection deprecation
            notification.icon = context.getApplicationInfo().icon;
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Resources resources = VirtualCore.get().getResources(packageName);
            Icon smallIcon = notification.getSmallIcon();
            if (smallIcon != null && 2 == (int) Reflect.on(smallIcon).get("mType")) {
                Reflect.on(smallIcon).set("mObj1", resources);
                Reflect.on(smallIcon).set("mString1", packageName);
            }
            Icon largeIcon = notification.getLargeIcon();
            if (largeIcon != null && 2 == (int) Reflect.on(largeIcon).get("mType")) {
                Reflect.on(largeIcon).set("mObj1", resources);
                Reflect.on(largeIcon).set("mString1", packageName);
            }
        }
    }


    static void fixIconImage(Resources resources, RemoteViews remoteViews, Notification notification) {
        if (remoteViews == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                //noinspection deprecation
                int id = R_Hide.id.icon.get();
                Drawable drawable = resources.getDrawable(notification.icon);
                drawable.setLevel(notification.iconLevel);
                Bitmap bitmap = drawableToBitMap(drawable);
//                Log.i(NotificationHandler.TAG, "den" + resources.getConfiguration().densityDpi);
                remoteViews.setImageViewBitmap(id, bitmap);
                if(Build.VERSION.SDK_INT >= 21) {
                    remoteViews.setInt(id, "setBackgroundColor", Color.TRANSPARENT);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    remoteViews.setViewPadding(id, 0, 0,0,0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                int id = R_Hide.id.icon.get();
                Icon icon = notification.getLargeIcon();
                if (icon == null) {
                    icon = notification.getSmallIcon();
                }
                remoteViews.setImageViewIcon(id, icon);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void fixNotificationIcon(Context context, Notification notification, Notification.Builder builder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            //noinspection deprecation
            builder.setSmallIcon(notification.icon);
            //noinspection deprecation
            builder.setLargeIcon(notification.largeIcon);
        } else {
            Icon icon = notification.getSmallIcon();
            if (icon != null) {
                Bitmap bitmap = drawableToBitMap(icon.loadDrawable(context));
                if (bitmap != null) {
                    Icon newIcon = Icon.createWithBitmap(bitmap);
                    builder.setSmallIcon(newIcon);
                }
            }
            Icon largeIcon = notification.getLargeIcon();
            if (largeIcon != null) {
                Bitmap bitmap = drawableToBitMap(largeIcon.loadDrawable(context));
                if (bitmap != null) {
                    Icon newIcon = Icon.createWithBitmap(bitmap);
                    builder.setLargeIcon(newIcon);
                }
            }
        }
    }

    private static Bitmap drawableToBitMap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
            return bitmapDrawable.getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }
}
