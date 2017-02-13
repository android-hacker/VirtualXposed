package com.lody.virtual.client.hook.patchs.notification.compat;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.graphics.drawable.ScaleDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.proto.AppSetting;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.VLog;

import java.io.IOException;

class NotificationHandlerCompatV21 extends NotificationHandlerCompatV14 {
    static final String TAG = NotificationHandlerCompatV21.class.getSimpleName();

    NotificationHandlerCompatV21() {
    }

    @Override
    public Result dealNotification(Context context, Notification notification, String packageName) throws Exception {
        VLog.i(TAG, "dealNotification:" + packageName + ",notification=" + notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean rs = resolveRemoteViews(context, packageName, notification);
            boolean rs2 = resolveRemoteViews(context, packageName, notification.publicVersion);
            if (rs || rs2) {
                Result result = new Result(RES_NOT_DEAL, null);
                result.code = RES_DEAL_OK;
                return result;
            }
        }
        return super.dealNotification(context, notification, packageName);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private boolean resolveRemoteViews(Context context, String packageName, Notification notification) {
        if (notification == null) {
            return false;
        }
//        Context pluginContext = getAppContext(context, packageName);
        Resources resources = VirtualCore.get().getResources(packageName);
        ApplicationInfo host = VirtualCore.get().getContext().getApplicationInfo();
        if (notification.contentView == null && notification.bigContentView == null) {
            Notification tmp = NotificationUtils.clone(context, notification);
            notification.contentView = tmp.contentView;
        }
//        notification.color = Color.BLACK;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (resources != null) {
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
        }else{
            //6.0以下的图标修复
            NotificationUtils.fixIconImage(resources, notification.tickerView, notification);
            NotificationUtils.fixIconImage(resources, notification.contentView, notification);
            NotificationUtils.fixIconImage(resources, notification.bigContentView, notification);
            NotificationUtils.fixIconImage(resources, notification.headsUpContentView, notification);
            notification.icon = host.icon;
        }
        //检查资源布局资源Id是否属于宿主
        //资源是来自插件
        AppSetting appSetting = VirtualCore.get().findApp(packageName);
        if (appSetting != null) {
            ApplicationInfo old = getApplication(notification);
            if (old == null) {
                try {
                    old = VirtualCore.getPM().getApplicationInfo(packageName, 0);
                } catch (PackageManager.NameNotFoundException e) {
                    old = host;
                }
            }
            if (!TextUtils.equals(old.publicSourceDir, appSetting.apkPath)) {
                ApplicationInfo proxyApplicationInfo = new ApplicationInfo(old);
                //                proxyApplicationInfo.packageName = VirtualCore.get().getHostPkg();
//                proxyApplicationInfo.uid = host.uid;
                //要确保publicSourceDir这个路径可以被SystemUI应用读取
//            proxyApplicationInfo.sourceDir = appSetting.apkPath;
//            proxyApplicationInfo.dataDir = appSetting.apkPath;
                proxyApplicationInfo.publicSourceDir = appSetting.apkPath;
                Log.w(TAG, "proxyApplicationInfo=" + proxyApplicationInfo + ",apk=" + appSetting.apkPath);
                fixApplication(notification.tickerView, proxyApplicationInfo);
                fixApplication(notification.contentView, proxyApplicationInfo);
                fixApplication(notification.bigContentView, proxyApplicationInfo);
                fixApplication(notification.headsUpContentView, proxyApplicationInfo);
                Bundle bundle = Reflect.on(notification).get("extras");
                if (bundle != null) {
                    bundle.putParcelable("android.appInfo", proxyApplicationInfo);
                }
            } else {
                Log.w(TAG, "old=" + old);
            }
            return true;
        }
        return false;
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
