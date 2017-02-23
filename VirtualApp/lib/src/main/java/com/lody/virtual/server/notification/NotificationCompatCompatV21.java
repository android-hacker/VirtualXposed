package com.lody.virtual.server.notification;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.ResourcesUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.server.pm.VAppManagerService;

class NotificationCompatCompatV21 extends NotificationCompatCompatV14 {
    static final String TAG = NotificationCompatCompatV21.class.getSimpleName();

    NotificationCompatCompatV21() {
        super();
    }

    @Override
    public boolean dealNotification(int id, Notification notification, String packageName) {
//        VLog.d(TAG, "dealNotification:" + packageName + ",notification=" + notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Context pluginContext = getAppContext(packageName);
            return  resolveRemoteViews(pluginContext, packageName, notification)
                    || resolveRemoteViews(pluginContext, packageName, notification.publicVersion);
        }
        return super.dealNotification(id, notification, packageName);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean resolveRemoteViews(Context pluginContext,String packageName, Notification notification) {
        if (notification == null) {
            return false;
        }
        String publicApk = null;
        PackageInfo packageInfo = getPackageInfo(packageName);
        ApplicationInfo host = getHostContext().getApplicationInfo();
        if (packageInfo != null) {
            publicApk = packageInfo.applicationInfo.publicSourceDir;
        }
        if (TextUtils.isEmpty(publicApk)) {
            publicApk = ResourcesUtils.getPackageResourcePath(packageName).getAbsolutePath();
        }

        //remoteviews
        getNotificationFixer().fixNotificationRemoteViews(pluginContext, notification);
        //图标修复
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getNotificationFixer().fixIcon(notification.getSmallIcon(), pluginContext, packageInfo!=null);
            getNotificationFixer().fixIcon(notification.getLargeIcon(), pluginContext, packageInfo!=null);
        }else{
            getNotificationFixer().fixIconImage(pluginContext.getResources(), notification.contentView, false, notification);
        }
        notification.icon = host.icon;

        ApplicationInfo proxyApplicationInfo = new ApplicationInfo(host);
        //要确保publicSourceDir这个路径可以被SystemUI应用读取
        proxyApplicationInfo.packageName = packageName;
        proxyApplicationInfo.publicSourceDir = publicApk;
        VLog.d(TAG, "proxyApplicationInfo=" + proxyApplicationInfo + ",apk=" + publicApk);

        fixApplication(notification.tickerView, proxyApplicationInfo);
        fixApplication(notification.contentView, proxyApplicationInfo);
        fixApplication(notification.bigContentView, proxyApplicationInfo);
        fixApplication(notification.headsUpContentView, proxyApplicationInfo);
        Bundle bundle = Reflect.on(notification).get("extras");
        if (bundle != null) {
            bundle.putParcelable(EXTRA_BUILDER_APPLICATION_INFO, proxyApplicationInfo);
        }
        return true;
    }

    private ApplicationInfo getApplication(Notification notification) {
        ApplicationInfo applicationInfo = getApplication(notification.tickerView);
        if (applicationInfo != null) {
            return applicationInfo;
        }
        applicationInfo = getApplication(notification.contentView);
        if (applicationInfo != null) {
            return applicationInfo;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            applicationInfo = getApplication(notification.bigContentView);
            if (applicationInfo != null) {
                return applicationInfo;
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            applicationInfo = getApplication(notification.headsUpContentView);
            if (applicationInfo != null) {
                return applicationInfo;
            }
        }
        return null;
    }

    private ApplicationInfo getApplication(RemoteViews remoteViews) {
        if (remoteViews == null) return null;
        return Reflect.on(remoteViews).get("mApplication");
    }

    private void fixApplication(RemoteViews remoteViews, ApplicationInfo applicationInfo) {
        if (remoteViews == null) return;
//        ArrayList<Object> mActions = Reflect.on(remoteViews).get("mActions");
//        if (mActions != null) {
//            remoteViews.setImageViewResource();
//            for (Object action : mActions) {
//
//            }
//        }
        Reflect.on(remoteViews).set("mApplication", applicationInfo);
    }
}
