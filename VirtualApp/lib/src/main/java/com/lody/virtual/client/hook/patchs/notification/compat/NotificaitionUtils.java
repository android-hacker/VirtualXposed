package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/***
 * 通知栏的包名判断
 */
/* package */ class NotificaitionUtils {
    private static Map<Integer, String> sSystemLayoutResIds = new HashMap<Integer, String>(0);

    static {
        init();
    }

    private static void init() {
        try {
            //read all com.android.internal.R
            Class clazz = Class.forName("com.android.internal.R$layout");
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                //public static final
                if (Modifier.isPublic(field.getModifiers()) && Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                    try {
                        int id = field.getInt(null);
                        sSystemLayoutResIds.put(id, field.getName());
                    } catch (IllegalAccessException e) {
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static boolean isSystemLayoutId(int id) {
        return sSystemLayoutResIds.containsKey(Integer.valueOf(id));
    }

    public static boolean isCustomNotification(Notification notification) {
        if (isCustomNotification(notification.contentView)) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (isCustomNotification(notification.tickerView)) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (isCustomNotification(notification.bigContentView)) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isCustomNotification(notification.headsUpContentView)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCustomNotification(RemoteViews remoteViews) {
        if (remoteViews == null) {
            return false;
        } else if (sSystemLayoutResIds.containsKey(remoteViews.getLayoutId())) {
            return false;
        } else {
            return true;
        }
    }

    //
    public static boolean isPluginNotification(Notification notification) {
        if (notification == null) {
            return false;
        }

        if (notification.contentView != null && !isHostPackageName(notification.contentView.getPackage())) {
            return true;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (notification.tickerView != null && !isHostPackageName(notification.tickerView.getPackage())) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (notification.bigContentView != null
                    && !isHostPackageName(notification.bigContentView.getPackage())) {
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (notification.headsUpContentView != null
                    && !isHostPackageName(notification.headsUpContentView.getPackage())) {
                return true;
            }
            if (notification.publicVersion != null
                    && notification.publicVersion.contentView != null
                    && !isHostPackageName(notification.publicVersion.contentView.getPackage())) {
                return true;
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getSmallIcon();
            if (icon != null) {
                try {
                    Object mString1Obj = Reflect.on(icon).get("mString1");
                    if (mString1Obj instanceof String) {
                        String mString1 = ((String) mString1Obj);
                        if (!isHostPackageName(mString1)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getLargeIcon();
            if (icon != null) {
                try {
                    Object mString1Obj = Reflect.on(icon).get("mString1");
                    if (mString1Obj instanceof String) {
                        String mString1 = ((String) mString1Obj);
                        if (!isHostPackageName(mString1)) {
                            return true;
                        }
                    }
                } catch (Exception e) {
                }
            }
        }

        try {
            Bundle mExtras = Reflect.on(notification).get("extras");
            for (String s : mExtras.keySet()) {
                if (mExtras.get(s) != null && mExtras.get(s) instanceof ApplicationInfo) {
                    ApplicationInfo applicationInfo = (ApplicationInfo) mExtras.get(s);
                    if (applicationInfo != null) {
                        return !isHostPackageName(applicationInfo.packageName);
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static Notification clone(Context context, Notification notification) {
        //插件的icon，绘制完成再替换成自己的
        Notification notification1 = null;
        //TODO 貌似克隆有问题,icon不对，如果不克隆，就得去找出title和contentText
        try {
            notification1 = new Notification();
            Reflect.on(notification).call("cloneInto", notification1, true);
        } catch (Exception e) {
            VLog.w("kk", "clone fail " + notification);
            notification1 = null;
        }
        if (notification1 == null) {
            final Notification.Builder builder = new Notification.Builder(context);
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
            ResourcesCompat.getInstance().fixNotificationIcon(context, notification, builder);
            if (Build.VERSION.SDK_INT >= 16) {
                notification1 = builder.build();
            } else {
                notification1 = builder.getNotification();
            }
            notification1.flags = notification.flags;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            notification1.extras.putParcelable("android.rebuild.applicationInfo", context.getApplicationInfo());
        }
        return notification1;
    }

    private static boolean isHostPackageName(String pkg) {
        return VirtualCore.getCore().isHostPackageName(pkg);
    }
}
