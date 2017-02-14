package com.lody.virtual.client.hook.patchs.notification.compat;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.InvocationTargetException;

/**
 * Created by 247321453 on 2016/7/12.
 */
public abstract class NotificationHandler {

    static final String TAG = NotificationHandler.class.getSimpleName();

    /**
     * Needn't deal the Notification
     */
    public static final int RES_NOT_DEAL = 0;
    /**
     * Need to replace the new Notification
     */
    public static final int RES_REPLACE = 1;
    /**
     * Notification Fixed, needn't replace.
     */
    public static final int RES_DEAL_OK = 2;
    /**
     * Needn't show the Notification
     */
    public static final int RES_NOT_SHOW = 3;

    @Deprecated
    public static boolean DEPEND_SYSTEM = true;

    private static final NotificationHandler sInstance;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sInstance = new NotificationHandlerCompatV21();
        } else {
            sInstance = new NotificationHandlerCompatV14();
        }
    }

    NotificationHandler() {
    }

    public static NotificationHandler getInstance() {
        return sInstance;
    }

    public abstract Result dealNotification(Context context, Notification notification, String packageName) throws Exception;
}
