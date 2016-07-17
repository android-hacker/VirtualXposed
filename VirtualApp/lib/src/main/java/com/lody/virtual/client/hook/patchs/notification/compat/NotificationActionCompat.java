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
import android.os.Build;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by 247321453 on 2016/7/17.
 * droidplugin项目的notification处理代码
 */
/*package*/ class NotificationActionCompat {
    private Map<Integer, String> sSystemLayoutResIds = new HashMap<Integer, String>(0);

    public boolean shouldBlock(Notification notification) {
        if (shouldBlockByRemoteViews(notification.contentView)) {
//            Log.d("kk", "shouldBlock contentView:" + notification.contentView.getClass().getName());
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            if (shouldBlockByRemoteViews(notification.tickerView)) {
//                Log.d("kk", "shouldBlock tickerView");
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (shouldBlockByRemoteViews(notification.bigContentView)) {
//                Log.d("kk", "shouldBlock bigContentView");
                return true;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (shouldBlockByRemoteViews(notification.headsUpContentView)) {
//                Log.d("kk", "shouldBlock headsUpContentView");
                return true;
            }
        }
        return false;
    }

    private boolean shouldBlockByRemoteViews(RemoteViews remoteViews) {
        if (remoteViews == null) {
//            Log.d("kk", "shouldBlockByRemoteViews is null");
            return false;
        } else if (sSystemLayoutResIds.containsKey(remoteViews.getLayoutId())) {
//            Log.d("kk", "shouldBlockByRemoteViews is systemId");
            return false;
        } else {
            return true;
        }
    }

    /*package*/ void init(Context context) {
        if (sSystemLayoutResIds.size() == 0) {
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
    }

    /*package*/ void hackNotification(Notification notification) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        if (Build.VERSION.SDK_INT >= 21) {
            if (notification.contentView == null && notification.publicVersion != null) {
                notification.contentView = notification.publicVersion.contentView;
                notification.flags = notification.publicVersion.flags;
                notification.category = notification.publicVersion.category;
                notification.actions = notification.publicVersion.actions;
                notification.sound = notification.publicVersion.sound;
                Reflect.on(notification).set("mGroupKey", notification.publicVersion.getGroup());
            }
            hackNotificationCompat(notification);
            hackNotificationCompat(notification.publicVersion);
        } else {
            hackNotificationCompat(notification);
        }
    }

    private void hackNotificationCompat(Notification notification) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        //        remoteViews com.android.internal.R.layout.notification_template_material_media
        //        com.android.internal.R.layout.notification_template_material_big_media_narrow;
        //        com.android.internal.R.layout.notification_template_material_big_media;
        //        //getBaseLayoutResource
        //        R.layout.notification_template_material_base;
        //        //getBigBaseLayoutResource
        //        R.layout.notification_template_material_big_base;
        //        //getBigPictureLayoutResource
        //        R.layout.notification_template_material_big_picture;
        //        //getBigTextLayoutResource
        //        R.layout.notification_template_material_big_text;
        //        //getInboxLayoutResource
        //        R.layout.notification_template_material_inbox;
        //        //getActionLayoutResource
        //        R.layout.notification_material_action;
        //        //getActionTombstoneLayoutResource
        //        R.layout.notification_material_action_tombstone;
        if (notification != null) {
            notification.icon = VirtualCore.getCore().getContext().getApplicationInfo().icon;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                hackRemoteViews(notification.tickerView);
            }
            hackRemoteViews(notification.contentView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                hackRemoteViews(notification.bigContentView);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hackRemoteViews(notification.headsUpContentView);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.graphics.drawable.Icon icon = notification.getSmallIcon();
                if (icon != null) {
                    Bitmap bitmap = drawableToBitMap(icon.loadDrawable(VirtualCore.getCore().getContext()));
                    if (bitmap != null) {
                        android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                        Reflect.on(notification).set("mSmallIcon", newIcon);
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                android.graphics.drawable.Icon icon = notification.getLargeIcon();
                if (icon != null) {
                    Bitmap bitmap = drawableToBitMap(icon.loadDrawable(VirtualCore.getCore().getContext()));
                    if (bitmap != null) {
                        android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                        Reflect.on(notification).set("mLargeIcon", newIcon);
                    }
                }
            }
        }
    }

    private void hackRemoteViews(RemoteViews remoteViews) throws IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
        if (remoteViews != null && !VirtualCore.getCore().isHostPackageName(remoteViews.getPackage())) {
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
                        if (sSystemLayoutResIds.containsKey(remoteViews.getLayoutId())) {
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
                        } else {
                            //TODO 自定义布局
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Reflect.on(remoteViews).set("mApplication", VirtualCore.getCore().getContext().getApplicationInfo());
            } else {
                Reflect.on(remoteViews).set("mPackage", VirtualCore.getCore().getContext().getPackageName());
            }
        }
    }

    public void builderNotificationIcon(Notification notification) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
//            final int icon = notification.icon;
            notification.icon = VirtualCore.getCore().getContext().getApplicationInfo().icon;
            //貌似得等通知栏显示后才能修改
//            try {
//                int id = Reflect.on("com.android.internal.R$id").get("icon");
//                Resources resources = VirtualCore.getCore().getResources(notification.contentView.getPackage());
//                Bitmap bitmap = drawableToBitMap(resources.getDrawable(icon));
//                if (notification.contentView != null) {
//                    notification.contentView.setImageViewBitmap(id, bitmap);
//                    Log.i("kk", "set icon ok:"+bitmap);
//                }else if(Build.VERSION.SDK_INT>=16 && notification.bigContentView!=null){
//                    notification.bigContentView.setImageViewBitmap(id, bitmap);
//                    Log.i("kk", "set icon ok");
//                }
//            } catch (Exception e) {
//                Log.e("kk", "icon", e);
//            }
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            android.graphics.drawable.Icon icon = notification.getSmallIcon();
            if (icon != null) {
                Bitmap bitmap = drawableToBitMap(icon.loadDrawable(VirtualCore.getCore().getContext()));
                if (bitmap != null) {
                    android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                    notification.setSmallIcon(newIcon);
                }
            }
            android.graphics.drawable.Icon icon2 = notification.getLargeIcon();
            if (icon2 != null) {
                Bitmap bitmap = drawableToBitMap(icon2.loadDrawable(VirtualCore.getCore().getContext()));
                if (bitmap != null) {
                    android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                    Reflect.on(notification).set("mLargeIcon", newIcon);
                }
            }
        }
    }

    public void builderNotificationIcon(Notification notification, Notification.Builder builder) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        android.graphics.drawable.Icon icon = notification.getSmallIcon();
        if (icon != null) {
            Bitmap bitmap = drawableToBitMap(icon.loadDrawable(VirtualCore.getCore().getContext()));
            if (bitmap != null) {
                android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                builder.setSmallIcon(newIcon);
            }
        }
        android.graphics.drawable.Icon icon2 = notification.getLargeIcon();
        if (icon2 != null) {
            Bitmap bitmap = drawableToBitMap(icon2.loadDrawable(VirtualCore.getCore().getContext()));
            if (bitmap != null) {
                android.graphics.drawable.Icon newIcon = android.graphics.drawable.Icon.createWithBitmap(bitmap);
                builder.setLargeIcon(newIcon);
            }
        }
    }

    public Bitmap drawableToBitMap(Drawable drawable) {
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
