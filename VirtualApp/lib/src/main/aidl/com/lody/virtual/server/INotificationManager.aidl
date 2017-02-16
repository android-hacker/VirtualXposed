// INotificationManager.aidl
package com.lody.virtual.server;

// Declare any non-default types here with import statements
import android.app.Notification;

interface INotificationManager {
    int dealNotificationId(int id, String packageName, String tag, int vuserId);
    String dealNotificationTag(int id, String packageName, String tag, int vuserId);
    boolean areNotificationsEnabledForPackage(String packageName, int vuserId);
    void setNotificationsEnabledForPackage(String packageName, boolean enable, int vuserId);
    void addNotification(int id, String tag, String packageName, int vuserId);
    void cancelAllNotification(String packageName, int vuserId);
}
