package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by 247321453 on 2016/7/12.
 */
public class NotificationHandler {
    private static final String TAG = NotificationHandler.class.getSimpleName();
    /** 双开不处理 */
    public static boolean DOPEN_NOT_DEAL = false;
    /** 系统样式的通知栏不处理 */
    public static boolean SYSTEM_NOTIFICATION_NOT_DEAL = false;
    /** 没有处理 */
    public static final int RESULT_CODE_DONT_DEAL = 0;
    /** 需要替换通知栏 */
    public static final int RESULT_CODE_REPLACE = 1;
    /** 替换了资源 */
    public static final int RESULT_CODE_DEAL_OK = 2;
    /** 不显示通知栏 */
    public static final int RESULT_CODE_DONT_SHOW = 3;

    static NotificationHandler sNotificationHandler;

    public static NotificationHandler getInstance() {
        if (sNotificationHandler == null) {
            synchronized (NotificationHandler.class) {
                if (sNotificationHandler == null) {
                    sNotificationHandler = new NotificationHandler();
                }
            }
        }
        return sNotificationHandler;
    }

    private NotificationHandler() {
    }

    public class Result {
        public int code;
        public Notification notification;

        public Result(int code, Notification notification) {
            this.code = code;
            this.notification = notification;
        }
    }

    private Context getContext() {
        return VirtualCore.getCore().getContext();
    }

    public Result dealNotification(Context context, Notification notification, String packageName) throws Exception {
        Result result = new Result(RESULT_CODE_DONT_DEAL, null);
        if (!NotificaitionUtils.isPluginNotification(notification)) {
            //不是插件的通知栏
            return result;
        }
        if (DOPEN_NOT_DEAL) {
            if (VirtualCore.getCore().isOutsideInstalled(packageName)) {
                //双开模式，直接替换icon
                ResourcesCompat.getInstance().fixNotificationIcon(context, notification);
                result.code = RESULT_CODE_DEAL_OK;
                return result;
            }
        }
        if (NotificaitionUtils.isCustomNotification(notification)) {
            //自定义样式
            Notification notification1 = replaceNotification(context, packageName, notification);
            if (notification1 != null) {
                result.code = RESULT_CODE_REPLACE;
                result.notification = notification1;
            } else {
                result.code = RESULT_CODE_DONT_SHOW;
            }
        } else {
            //系统样式
            if (!SYSTEM_NOTIFICATION_NOT_DEAL) {
                Notification notification1 = replaceNotification(context, packageName, notification);
                if (notification1 != null) {
                    result.code = RESULT_CODE_REPLACE;
                    result.notification = notification1;
                }else {
                    result.code = RESULT_CODE_DONT_SHOW;
                }
            }
        }
        if (result.code != RESULT_CODE_DONT_SHOW && result.notification == null) {
            ResourcesCompat.getInstance().fixNotificationResource(notification);
            result.code = RESULT_CODE_DEAL_OK;
        }
        return result;
    }

    /***
     * @param packageName  通知栏包名
     * @param notification 通知栏
     * @return
     * @throws PackageManager.NameNotFoundException
     */
    private Notification replaceNotification(Context context, String packageName, Notification notification) throws PackageManager.NameNotFoundException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Context pluginContext = getContext(context, packageName);
        if (pluginContext == null) {
            return null;
        }
        //获取需要绘制的remoteviews
        RemoteViewsCompat remoteViewsCompat = new RemoteViewsCompat(pluginContext, notification);
        ///clone and set
        Notification notification1 = NotificaitionUtils.clone(pluginContext, notification);
        //貌似没啥用
        if (Build.VERSION.SDK_INT >= 16) {
            RemoteViews oldBigContentViews = remoteViewsCompat.getBigRemoteViews();
            ResourcesCompat.getInstance().fixIconImage(pluginContext, oldBigContentViews, notification);
            RemoteViews bigContentViews = RemoteViewsUtils.getInstance().createViews(context, pluginContext, oldBigContentViews, true);
            notification1.bigContentView = bigContentViews;
        }

        RemoteViews oldContentView = remoteViewsCompat.getRemoteViews();
        ResourcesCompat.getInstance().fixIconImage(pluginContext, oldContentView, notification);
        RemoteViews contentViews = RemoteViewsUtils.getInstance().createViews(context, pluginContext, oldContentView, false);
        notification1.contentView = contentViews;
        ///TODO 其他contentViews？
        ResourcesCompat.getInstance().fixNotificationIcon(context, notification1);
//        ResourcesCompat.getInstance().fixNotificationResource(notification1);
        return notification1;
    }

    private Context getContext(Context base, String packageName) {
        Context pluginContext = null;
        try {
            pluginContext = base.createPackageContext(packageName,
                    Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return pluginContext;
    }
}
