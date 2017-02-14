package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Looper;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by 247321453 on 2016/7/12.
 */
 class NotificationHandlerCompatV14 extends NotificationHandler {


    NotificationHandlerCompatV14() {
    }

    @Override
    public Result dealNotification(Context context, Notification notification, String packageName) throws Exception {
        Result result = new Result(RES_NOT_DEAL, null);
        if (DEPEND_SYSTEM) {
            if (VirtualCore.get().isOutsideInstalled(packageName)) {
                //外部的app
                NotificationUtils.fixNotificationIcon(context, notification, packageName);
                result.code = RES_DEAL_OK;
                return result;
            }
        }
        Notification replaceNotification = replaceNotification(context, packageName, notification);
        if (replaceNotification != null) {
            result.code = RES_REPLACE;
            result.notification = replaceNotification;
        } else {
            result.code = RES_NOT_SHOW;
        }
        return result;
    }

    private Notification replaceNotification(Context context, String packageName, Notification notification)
            throws PackageManager.NameNotFoundException, ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        Resources resources = VirtualCore.get().getResources(packageName);
        Context appContext = getAppContext(context, packageName);
        if (appContext == null) {
            return null;
        }
        RemoteViewsCompat remoteViewsCompat = new RemoteViewsCompat(appContext, notification);
        Notification cloneNotification = NotificationUtils.clone(appContext, notification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            RemoteViews oldHeadsUpContentView = remoteViewsCompat.getHeadsUpContentView();
            NotificationUtils.fixIconImage(resources, oldHeadsUpContentView, notification);
            cloneNotification.headsUpContentView = RemoteViewsUtils.getInstance().createViews(context, appContext,
                    oldHeadsUpContentView, false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            RemoteViews oldBigContentViews = remoteViewsCompat.getBigRemoteViews();
            NotificationUtils.fixIconImage(resources, oldBigContentViews, notification);
            cloneNotification.bigContentView = RemoteViewsUtils.getInstance().createViews(context, appContext,
                    oldBigContentViews, true);
        }
        RemoteViews oldContentView = remoteViewsCompat.getRemoteViews();
        NotificationUtils.fixIconImage(resources, oldContentView, notification);
        cloneNotification.contentView = RemoteViewsUtils.getInstance().createViews(context, appContext, oldContentView,
                false);
        NotificationUtils.fixNotificationIcon(context, cloneNotification, packageName);
        return cloneNotification;

    }

    Context getAppContext(Context base, String packageName) {
        try {
            return base.createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

}
