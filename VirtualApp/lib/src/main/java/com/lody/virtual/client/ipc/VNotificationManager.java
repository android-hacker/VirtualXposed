package com.lody.virtual.client.ipc;

import android.app.Notification;
import android.os.IBinder;
import android.os.RemoteException;

import com.lody.virtual.server.INotificationManager;
import com.lody.virtual.server.notification.NotificationCompat;

/**
 * Fake notification manager
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
                    final IBinder pmBinder = ServiceManagerNative.getService(ServiceManagerNative.NOTIFICATION);
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

    public boolean areNotificationsEnabledForPackage(String packageName, int userId) {
        try {
            return getService().areNotificationsEnabledForPackage(packageName, userId);
        } catch (RemoteException e) {
            e.printStackTrace();
            return true;
        }
    }

    public void setNotificationsEnabledForPackage(String packageName, boolean enable, int userId) {
        try {
            getService().setNotificationsEnabledForPackage(packageName, enable, userId);
        } catch (RemoteException e) {
        }
    }

    public void addNotification(int id, String tag, String packageName, int userId) {
        try {
            getService().addNotification(id, tag, packageName, userId);
        } catch (RemoteException e) {
        }
    }

    public void cancelAllNotification(String packageName, int userId) {
        try {
            getService().cancelAllNotification(packageName, userId);
        } catch (RemoteException e) {
        }
    }
}
