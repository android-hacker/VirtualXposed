package com.lody.virtual.client.ipc;

import android.app.Notification;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.server.INotificationManager;
import com.lody.virtual.server.IPackageManager;
import com.lody.virtual.server.notification.NotificationCompat;

/**
 * 通知栏管理，多虚拟用户，多包名，但是总通知栏只能显示255个，系统限制
 */
public class VNotificationManager {
    private static final VNotificationManager sMgr = new VNotificationManager();
    private INotificationManager mRemote;
    private NotificationCompat mNotificationCompat;

    private VNotificationManager() {
        mNotificationCompat = NotificationCompat.create();
    }

    public static VNotificationManager get() {
        return sMgr;
    }

    public INotificationManager getService() {
        if (mRemote == null) {
            synchronized (VNotificationManager.class) {
                if (mRemote == null) {
                    final IBinder pmBinder = ServiceManagerNative.getService(ServiceManagerNative.VIRTUAL_NOTIFICATION);
                    mRemote = INotificationManager.Stub.asInterface(pmBinder);
                }
            }
        }
        return mRemote;
    }

    public boolean dealNotification(int id, Notification notification, String packageName) {
        if (mNotificationCompat.getHostContext().getPackageName().equals(packageName)) {
            return true;
        }
        return mNotificationCompat.dealNotification(id, notification, packageName);
    }

    public int dealNotificationId(int id, String packageName, String tag, int userId) {
        //不处理id，通过tag处理
        try {
            return getService().dealNotificationId(id, packageName, tag, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return id;
        }
    }

    public String dealNotificationTag(int id, String packageName, String tag, int userId) {
        try {
            return getService().dealNotificationTag(id, packageName, tag, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return tag;
        }
    }

    public boolean areNotificationsEnabledForPackage(String packageName, int vuserId) {
        try {
            return getService().areNotificationsEnabledForPackage(packageName, vuserId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void setNotificationsEnabledForPackage(String packageName, boolean enable, int vuserId) {
        try {
            getService().setNotificationsEnabledForPackage(packageName, enable, vuserId);
        } catch (RemoteException e) {
        }
    }

    public void addNotification(int id, String tag, String packageName, int vuserId) {
        try {
            getService().addNotification(id, tag, packageName, vuserId);
        } catch (RemoteException e) {
        }
    }

    public void cancelAllNotification(String packageName, int vuserId) {
        try {
            getService().cancelAllNotification(packageName, vuserId);
        } catch (RemoteException e) {
        }
    }
}
