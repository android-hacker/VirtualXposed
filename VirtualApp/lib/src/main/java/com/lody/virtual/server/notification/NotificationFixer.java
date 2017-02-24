package com.lody.virtual.server.notification;

import android.annotation.TargetApi;
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
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;

import java.util.ArrayList;
import java.util.List;

import mirror.com.android.internal.R_Hide;

/* package */ class NotificationFixer {

    private static final String TAG = NotificationCompat.TAG;
    private NotificationCompat mNotificationCompat;

    NotificationFixer(NotificationCompat notificationCompat) {
        this.mNotificationCompat = notificationCompat;
    }

    @TargetApi(Build.VERSION_CODES.M)
    void fixIcon(Icon icon, Context pluginContext, boolean isInstall) {
        if (icon == null) return;
        int type = Reflect.on(icon).get("mType");
        if (type == 2) {
            if (isInstall) {
                Reflect.on(icon).set("mObj1", pluginContext.getResources());
                Reflect.on(icon).set("mString1", pluginContext.getPackageName());
            } else {
                Drawable drawable = icon.loadDrawable(pluginContext);
                Bitmap bitmap = drawableToBitMap(drawable);
                Reflect.on(icon).set("mObj1", bitmap);
                Reflect.on(icon).set("mString1", null);
                Reflect.on(icon).set("mType", 1);
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void fixNotificationRemoteViews(Context pluginContext, Notification notification) {
        Notification rebuild = null;
        try {
            rebuild = Reflect.on(Notification.Builder.class).create(pluginContext, notification).get();
        } catch (Exception e) {
            // ignore
        }
        if (rebuild != null) {
            if (notification.tickerView == null) {
                notification.tickerView = rebuild.tickerView;
            }
            if (notification.contentView == null) {
                notification.contentView = rebuild.contentView;
            }
            if (notification.bigContentView == null) {
                notification.bigContentView = rebuild.bigContentView;
            }
            if (notification.headsUpContentView == null) {
                notification.headsUpContentView = rebuild.headsUpContentView;
            }
        }
    }

    boolean fixRemoteViewActions(Context pluginContext, boolean isinstall, final RemoteViews remoteViews) {
        boolean hasIcon = false;
        if (remoteViews != null) {
            int systemIconViewId = R_Hide.id.icon.get();
            List<BitmapReflectionAction> mNew = new ArrayList<>();
            ArrayList<Object> mActions = Reflect.on(remoteViews).get("mActions");
            if (mActions != null) {
                int count = mActions.size();
                for (int i = count - 1; i >= 0; i--) {
                    Object action = mActions.get(i);
                    if (action == null) {
                        continue;
                    }
                    //TextViewDrawableAction
                    //setImageURI
                    //setLabelFor
                    if (action.getClass().getSimpleName().endsWith("TextViewDrawableAction")) {
                        mActions.remove(action);
                        continue;
                    }
                    if (ReflectionActionCompat.isInstance(action)
                            || (action.getClass().getSimpleName().endsWith("ReflectionAction"))) {
                        int viewId = Reflect.on(action).get("viewId");

                        String methodName = Reflect.on(action).get("methodName");
                        int type = Reflect.on(action).get("type");
                        Object value = Reflect.on(action).get("value");
                        if (!hasIcon) {
                            hasIcon = viewId == systemIconViewId;
                            if (hasIcon) {
                                if (type == ReflectionActionCompat.INT && (int) value == 0) {
                                    hasIcon = false;
                                }
                                if (hasIcon) {
                                    VLog.v(TAG, "find icon " + methodName + " type=" + type + ", value=" + value);
                                }
                            }
                        }
                        if (methodName.equals("setImageResource")) {
                            //setImageBitmap
                            mNew.add(new BitmapReflectionAction(viewId, "setImageBitmap",
                                    drawableToBitMap(pluginContext.getResources().getDrawable((int) value))));
                            mActions.remove(action);
                        } else if (methodName.equals("setText") && type == ReflectionActionCompat.INT) {
                            //setText string
                            Reflect.on(action).set("type", ReflectionActionCompat.STRING);
                            Reflect.on(action).set("value", pluginContext.getResources().getString((int) value));
                        } else if (methodName.equals("setLabelFor")) {
                            //TODO remove
                            mActions.remove(action);
                        } else if (methodName.equals("setBackgroundResource")) {
                            //TODO remove
                            mActions.remove(action);
                        } else if (methodName.equals("setImageURI")) {
                            Uri uri = (Uri) value;
                            if (!uri.getScheme().startsWith("http")) {
                                mActions.remove(action);
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                if (value instanceof Icon) {
                                    Icon icon = (Icon) value;
                                    fixIcon(icon, pluginContext, isinstall);
                                }
                            }
                        }
                    }
                }
                for (BitmapReflectionAction baction : mNew) {
                    remoteViews.setBitmap(baction.viewId, baction.methodName, baction.bitmap);
                }
            }
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                Reflect.on(remoteViews).set("mPackage", mNotificationCompat.getHostContext().getPackageName());
            }
        }
        return hasIcon;
    }

    private static class BitmapReflectionAction {
        int viewId;
        String methodName;
        Bitmap bitmap;

        BitmapReflectionAction(int viewId, String methodName, Bitmap bitmap) {
            this.viewId = viewId;
            this.methodName = methodName;
            this.bitmap = bitmap;
        }
    }

    void fixIconImage(Resources resources, RemoteViews remoteViews, boolean hasIconBitmap, Notification notification) {
        if (remoteViews == null) return;
        if (!mNotificationCompat.isSystemLayout(remoteViews)) {
            VLog.w(TAG, "ignore not system contentView");
            return;
        }
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        try {
            //noinspection deprecation
            int id = R_Hide.id.icon.get();
            if (!hasIconBitmap) {
                Drawable drawable = resources.getDrawable(android.R.drawable.sym_def_app_icon);//notification.icon);
                drawable.setLevel(notification.iconLevel);
                Bitmap bitmap = drawableToBitMap(drawable);
//                Log.i(NotificationHandler.TAG, "den" + resources.getConfiguration().densityDpi);
                remoteViews.setImageViewBitmap(id, bitmap);
            }
            if (Build.VERSION.SDK_INT >= 21) {
                remoteViews.setInt(id, "setBackgroundColor", Color.TRANSPARENT);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                remoteViews.setViewPadding(id, 0, 0, 0, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            VLog.w(TAG, "fix icon", e);
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
