package com.lody.virtual.server.notification;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.lody.virtual.client.core.VirtualCore;

/**
 * @author 247321543
 */
@SuppressWarnings("deprecation")
class NotificationCompatCompatV14 extends NotificationCompat {
    private final RemoteViewsFixer mRemoteViewsFixer;

    NotificationCompatCompatV14() {
        super();
        mRemoteViewsFixer = new RemoteViewsFixer(this);
    }

    private RemoteViewsFixer getRemoteViewsFixer() {
        return mRemoteViewsFixer;
    }

    @Override
    public boolean dealNotification(int id, Notification notification, final String packageName) {
        Context appContext = getAppContext(packageName);
        if (appContext == null) {
            return false;
        }
        if (VirtualCore.get().isOutsideInstalled(packageName)) {
            if(notification.icon != 0) {
                getNotificationFixer().fixIconImage(appContext.getResources(), notification.contentView, false, notification);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getNotificationFixer().fixIconImage(appContext.getResources(), notification.bigContentView, false, notification);
                }
                notification.icon = getHostContext().getApplicationInfo().icon;
            }
            return true;
        }
        if (notification.tickerView != null) {

            if (isSystemLayout(notification.tickerView)) {
                getNotificationFixer().fixRemoteViewActions(appContext, false, notification.tickerView);
            } else {
                notification.tickerView = getRemoteViewsFixer().makeRemoteViews(id + ":tickerView", appContext,
                        notification.tickerView, false, false);
            }
        }
        if (notification.contentView != null) {
            if (isSystemLayout(notification.contentView)) {
                boolean hasIconBitmap = getNotificationFixer().fixRemoteViewActions(appContext, false, notification.contentView);
                getNotificationFixer().fixIconImage(appContext.getResources(), notification.contentView, hasIconBitmap, notification);
            } else {
                notification.contentView = getRemoteViewsFixer().makeRemoteViews(id + ":contentView", appContext,
                        notification.contentView, false, true);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (notification.bigContentView != null) {
                if (isSystemLayout(notification.bigContentView)) {
                    getNotificationFixer().fixRemoteViewActions(appContext, false, notification.bigContentView);
                } else {
                    notification.bigContentView = getRemoteViewsFixer().makeRemoteViews(id + ":bigContentView", appContext,
                            notification.bigContentView, true, true);
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (notification.headsUpContentView != null) {
                if (isSystemLayout(notification.headsUpContentView)) {
                    boolean hasIconBitmap = getNotificationFixer().fixRemoteViewActions(appContext, false, notification.headsUpContentView);
                    getNotificationFixer().fixIconImage(appContext.getResources(), notification.contentView, hasIconBitmap, notification);
                } else {
                    notification.headsUpContentView = getRemoteViewsFixer().makeRemoteViews(id + ":headsUpContentView", appContext,
                            notification.headsUpContentView, false, false);
                }
            }
        }
        if(notification.icon != 0) {
            notification.icon = getHostContext().getApplicationInfo().icon;
        }
        return true;
    }

    Context getAppContext(final String packageName) {
        Context context = null;
        try {
            context = getHostContext().createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
           e.printStackTrace();
        }
        return context;
    }

}
