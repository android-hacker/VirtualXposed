package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by 247321453 on 2016/7/17.
 * droidplugin项目的notification处理代码
 */
/*package*/ class ResourcesCompat {
    private ResourcesCompat() {

    }

    private static ResourcesCompat sResourcesCompat;

    public static ResourcesCompat getInstance() {
        if (sResourcesCompat == null) {
            synchronized (ResourcesCompat.class) {
                if (sResourcesCompat == null) {
                    sResourcesCompat = new ResourcesCompat();
                }
            }
        }
        return sResourcesCompat;
    }

    public void fixNotificationResource(Notification notification) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        if (Build.VERSION.SDK_INT >= 21) {
            if (notification.contentView == null && notification.publicVersion != null) {
                notification.contentView = notification.publicVersion.contentView;
                notification.flags = notification.publicVersion.flags;
                notification.category = notification.publicVersion.category;
                notification.actions = notification.publicVersion.actions;
                notification.sound = notification.publicVersion.sound;
                Reflect.on(notification).set("mGroupKey", notification.publicVersion.getGroup());
            }
            fixNotificationCompat(notification);
            fixNotificationCompat(notification.publicVersion);
        } else {
            fixNotificationCompat(notification);
        }
    }

    private void fixNotificationCompat(Notification notification) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        if (notification != null) {
            fixNotificationIcon(notification);
            fixRemoteViews(notification.contentView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                fixRemoteViews(notification.tickerView);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                fixRemoteViews(notification.bigContentView);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                fixRemoteViews(notification.headsUpContentView);
            }
        }
    }

    public void fixNotificationIcon(Notification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            notification.icon = getContext().getApplicationInfo().icon;
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getSmallIcon();
            if (icon != null) {
                Bitmap bitmap = drawableToBitMap(icon.loadDrawable(getContext()));
                if (bitmap != null) {
                    android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                    Reflect.on(notification).call("setSmallIcon", newIcon);
                }
            }
            android.graphics.drawable.Icon icon2 = notification.getLargeIcon();
            if (icon2 != null) {
                Bitmap bitmap = drawableToBitMap(icon2.loadDrawable(getContext()));
                if (bitmap != null) {
                    android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                    Reflect.on(notification).call("mLargeIcon", newIcon);
                }
            }
        }
    }

    public void fixIconImage(Context pluginContext, RemoteViews remoteViews, Notification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                int iconId = notification.icon;
                int id = Reflect.on("com.android.internal.R$id").get("icon");
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= 21) {
                    bitmap = drawableToBitMap(pluginContext.getResources().getDrawable(iconId, pluginContext.getTheme()));
                } else {
                    bitmap = drawableToBitMap(pluginContext.getResources().getDrawable(iconId));
                }
                remoteViews.setImageViewBitmap(id, bitmap);
            } catch (Exception e) {
                VLog.e("kk", "set icon " + e);
            }
        } else {
            try {
                int id = Reflect.on("com.android.internal.R$id").get("icon");
                Icon icon = notification.getLargeIcon();
                if (icon == null) {
                    icon = notification.getSmallIcon();
                }
                remoteViews.setImageViewIcon(id, icon);
            } catch (Exception e) {
                VLog.e("kk", "set icon 23 " + e);
            }
        }
    }

    public void fixNotificationIcon(Context context, Notification notification, Notification.Builder builder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            builder.setSmallIcon(notification.icon);
            builder.setLargeIcon(notification.largeIcon);
        } else {
            android.graphics.drawable.Icon icon = notification.getSmallIcon();
            if (icon != null) {
                Bitmap bitmap = drawableToBitMap(icon.loadDrawable(context));
                if (bitmap != null) {
                    android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                    builder.setSmallIcon(newIcon);
                }
            }
            android.graphics.drawable.Icon icon2 = notification.getLargeIcon();
            if (icon2 != null) {
                Bitmap bitmap = drawableToBitMap(icon2.loadDrawable(context));
                if (bitmap != null) {
                    android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                    builder.setLargeIcon(newIcon);
                }
            }
        }
    }

    private void fixRemoteViews(RemoteViews remoteViews) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        if (remoteViews != null && !TextUtils.equals(getContext().getPackageName(), remoteViews.getPackage())) {
            Object mActionsObj = Reflect.on(remoteViews).get("mActions");
            if (mActionsObj instanceof Collection) {
                Collection mActions = (Collection) mActionsObj;
                String aPackage = remoteViews.getPackage();
                Resources resources = VirtualCore.getCore().getResources(aPackage);
                if (resources != null) {
                    Iterator iterable = mActions.iterator();
                    Class TextViewDrawableActionClass = null;
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            TextViewDrawableActionClass = Class.forName(RemoteViews.class.getName() + "$TextViewDrawableAction");
                        }
                    } catch (ClassNotFoundException e) {
                    }
                    Class ReflectionActionClass = Class.forName(RemoteViews.class.getName() + "$ReflectionAction");
                    while (iterable.hasNext()) {
                        Object action = iterable.next();
                        if (NotificaitionUtils.isSystemLayoutId(remoteViews.getLayoutId())) {
                            if (ReflectionActionClass.isInstance(action)) {//???这里这样是对的么？
                                String methodName = Reflect.on(action).get("methodName");
                                //String methodName;,int type; Object value;
                                if ("setImageResource".equals(methodName)) { //setInt(viewId, "setImageResource", srcId);
                                    Object BITMAP = Reflect.on(action.getClass()).get("BITMAP");
                                    int resId = Reflect.on(action).get("value");
                                    Bitmap bitmap = BitmapFactory.decodeResource(resources, resId);
                                    Reflect.on(action).set("type", BITMAP);
                                    Reflect.on(action).set("value", bitmap);
                                    Reflect.on(action).set("methodName", "setImageBitmap");
                                } else if ("setImageURI".equals(methodName)) {//setUri(viewId, "setImageURI", uri);
                                    iterable.remove();   //TODO RemoteViews.setImageURI 其实应该适配的。
                                } else if ("setLabelFor".equals(methodName)) {
                                    iterable.remove();   //TODO RemoteViews.setLabelFor 其实应该适配的。
                                }
                            } else if (TextViewDrawableActionClass != null && TextViewDrawableActionClass.isInstance(action)) {
                                iterable.remove();
//                                if ("setTextViewCompoundDrawables".equals(methodName)) {
//                                    iterable.remove();   //TODO RemoteViews.setTextViewCompoundDrawables 其实应该适配的。
//                                } else if ("setTextViewCompoundDrawablesRelative".equals(methodName)) {
//                                    iterable.remove();   //TODO RemoteViews.setTextViewCompoundDrawablesRelative 其实应该适配的。
//                                }
                            }
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Reflect.on(remoteViews).set("mApplication", getContext().getApplicationInfo());
            } else {
                Reflect.on(remoteViews).set("mPackage", getContext().getPackageName());
            }
        }
    }

    private Context getContext() {
        return VirtualCore.getCore().getContext();
    }

    private Bitmap drawableToBitMap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = ((BitmapDrawable) drawable);
            return bitmapDrawable.getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }
}
