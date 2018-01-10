package com.lody.virtual.server.interfaces;

import android.os.RemoteException;

/**
 * @author Lody
 */
public interface INotificationManager {

    int dealNotificationId(int id, String packageName, String tag, int userId) throws RemoteException;

    String dealNotificationTag(int id, String packageName, String tag, int userId) throws RemoteException;

    boolean areNotificationsEnabledForPackage(String packageName, int userId) throws RemoteException;

    void setNotificationsEnabledForPackage(String packageName, boolean enable, int userId) throws RemoteException;

    void addNotification(int id, String tag, String packageName, int userId) throws RemoteException;

    void cancelAllNotification(String packageName, int userId) throws RemoteException;
}