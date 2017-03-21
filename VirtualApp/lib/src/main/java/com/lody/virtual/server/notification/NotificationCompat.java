package com.lody.virtual.server.notification;

import android.app.Notification;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.RemoteViews;

import com.lody.virtual.client.core.VirtualCore;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import mirror.com.android.internal.R_Hide;

/**
 * @author 247321453
 */
public abstract class NotificationCompat {

    public static final String EXTRA_TITLE = "android.title";
    public static final String EXTRA_TITLE_BIG = EXTRA_TITLE + ".big";
    public static final String EXTRA_TEXT = "android.text";
    public static final String EXTRA_SUB_TEXT = "android.subText";
    public static final String EXTRA_INFO_TEXT = "android.infoText";
    public static final String EXTRA_SUMMARY_TEXT = "android.summaryText";
    public static final String EXTRA_BIG_TEXT = "android.bigText";
    public static final String EXTRA_PROGRESS = "android.progress";
    public static final String EXTRA_PROGRESS_MAX = "android.progressMax";
    public static final String EXTRA_BUILDER_APPLICATION_INFO = "android.appInfo";
    static final String TAG = NotificationCompat.class.getSimpleName();
    static final String SYSTEM_UI_PKG = "com.android.systemui";
    private final List<Integer> sSystemLayoutResIds = new ArrayList<>(10);
    private NotificationFixer mNotificationFixer;

    NotificationCompat() {
        loadSystemLayoutRes();
        mNotificationFixer = new NotificationFixer(this);
    }

    public static NotificationCompat create() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return new NotificationCompatCompatV21();
        } else {
            return new NotificationCompatCompatV14();
        }
    }

    private void loadSystemLayoutRes() {
        Field[] fields = R_Hide.layout.TYPE.getFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())) {
                try {
                    int id = field.getInt(null);
                    sSystemLayoutResIds.add(id);
                } catch (Throwable e) {
                    // ignore
                }
            }
        }
    }

    NotificationFixer getNotificationFixer() {
        return mNotificationFixer;
    }

    boolean isSystemLayout(RemoteViews remoteViews) {
        return remoteViews != null
                && sSystemLayoutResIds.contains(remoteViews.getLayoutId());
    }

    public Context getHostContext() {
        return VirtualCore.get().getContext();
    }

    PackageInfo getPackageInfo(String packageName) {
        try {
            return VirtualCore.get().getUnHookPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            // ignore
        }
        return null;
    }

    public abstract boolean dealNotification(int id, Notification notification, String packageName);
}
