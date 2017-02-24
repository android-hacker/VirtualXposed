package com.lody.virtual.server.notification;

import android.app.Notification;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;

import com.lody.virtual.helper.utils.VLog;

/**
 * @author 247321543
 */
/* package */ class NotificationCompatCompatV14 extends NotificationCompat {
    private RemoteViewsFixer mRemoteViewsFixer;

    NotificationCompatCompatV14() {
        super();
        mRemoteViewsFixer = new RemoteViewsFixer(this);
    }

    RemoteViewsFixer getRemoteViewsFixer() {
        return mRemoteViewsFixer;
    }

    @Override
    public boolean dealNotification(int id, Notification notification, final String packageName) {
        Context pluginContext = getAppContext(packageName);
        if (isOutsideInstalled(packageName)) {
            getNotificationFixer().fixIconImage(pluginContext.getResources(), notification.contentView, false, notification);
            notification.icon = getHostContext().getApplicationInfo().icon;
            return true;
        }
        if (notification.tickerView != null) {
            if (isSystemLayout(notification.tickerView)) {
                VLog.d(TAG, "deal system tickerView");
                getNotificationFixer().fixRemoteViewActions(pluginContext, false, notification.tickerView);
            } else {
                VLog.d(TAG, "deal custom tickerView " + notification.tickerView.getLayoutId());
                notification.tickerView = getRemoteViewsFixer().makeRemoteViews(id + ":tickerView", pluginContext,
                        notification.tickerView, false, false);
            }
        }
        if (notification.contentView != null) {
            if (isSystemLayout(notification.contentView)) {
                VLog.d(TAG, "deal system contentView");
                boolean hasIconBitmap = getNotificationFixer().fixRemoteViewActions(pluginContext, false, notification.contentView);
                getNotificationFixer().fixIconImage(pluginContext.getResources(), notification.contentView, hasIconBitmap, notification);
            } else {
                VLog.d(TAG, "deal custom contentView " + notification.contentView.getLayoutId());
                notification.contentView = getRemoteViewsFixer().makeRemoteViews(id + ":contentView", pluginContext,
                        notification.contentView, false, true);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (notification.bigContentView != null) {
                if (isSystemLayout(notification.bigContentView)) {
                    VLog.d(TAG, "deal system bigContentView");
                    getNotificationFixer().fixRemoteViewActions(pluginContext, false, notification.bigContentView);
                } else {
                    VLog.d(TAG, "deal custom bigContentView " + notification.bigContentView.getLayoutId());
                    notification.bigContentView = getRemoteViewsFixer().makeRemoteViews(id + ":bigContentView", pluginContext,
                            notification.bigContentView, true, true);
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (notification.headsUpContentView != null) {
                if (isSystemLayout(notification.headsUpContentView)) {
                    VLog.d(TAG, "deal system headsUpContentView");
                    getNotificationFixer().fixRemoteViewActions(pluginContext, false, notification.headsUpContentView);
                } else {
                    VLog.d(TAG, "deal custom headsUpContentView " + notification.bigContentView.getLayoutId());
                    notification.headsUpContentView = getRemoteViewsFixer().makeRemoteViews(id + ":headsUpContentView", pluginContext,
                            notification.headsUpContentView, false, false);
                }
            }
        }
        notification.icon = getHostContext().getApplicationInfo().icon;
        return true;
    }

    Context getAppContext(final String packageName) {
        final Resources resources = getResources(packageName);
        Context context = null;
        try {
            context = getHostContext().createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
            context = getHostContext();
            // ignore
        }
        return new ContextWrapper(context) {
            @Override
            public Resources getResources() {
                return resources;
            }

            @Override
            public String getPackageName() {
                return packageName;
            }
        };
    }

}
