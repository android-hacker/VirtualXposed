package com.lody.virtual.client.ipc;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;

import com.lody.virtual.client.ipc.notification.NotificationCompat;
import com.lody.virtual.helper.utils.VLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class VNotificationManager {
    static VNotificationManager sVNotificationManager = new VNotificationManager();
    private NotificationManager mNotificationManager;
    static final String TAG = NotificationCompat.class.getSimpleName();
    private final List<String> mDisables = new ArrayList<>();
    //需要保存
    private final HashMap<String, List<NotificationInfo>> mNotifications = new HashMap<>();
    private NotificationCompat mNotificationCompat;

    private VNotificationManager() {
        mNotificationCompat = NotificationCompat.create();
    }

    public void init(Context context) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    public boolean dealNotification(int id, Notification notification, String packageName) {
        if(mNotificationCompat.getHostContext().getPackageName().equals(packageName)){
            return true;
        }
        return mNotificationCompat.dealNotification(id, notification, packageName);
    }

    /***
     * 处理通知栏id
     *
     * @param id
     * @param packageName
     * @return
     */
    public int dealNotificationId(int id, String packageName, String tag, int userId) {
        //不处理id，通过tag处理
        return id;
    }

    /***
     * 处理通知栏id
     *
     * @param id
     * @param packageName
     * @return
     */
    public String dealNotificationTag(int id, String packageName, String tag, int userId) {
        //最好是知道vuserid
        if(TextUtils.equals(mNotificationCompat.getHostContext().getPackageName(), packageName)){
            return tag;
        }
        if (tag == null) {
            return packageName + "@" + userId;
        }
        return packageName + ":" + tag + "@" + userId;
    }

    public boolean areNotificationsEnabledForPackage(String packageName, int vuserId) {
        //最好是知道vuserid
        return !mDisables.contains(packageName + ":" + vuserId);
    }

    public void setNotificationsEnabledForPackage(String packageName, boolean enable, int vuserId) {
        String key = packageName + ":" + vuserId;
        //最好是知道vuserid
        if (enable) {
            if (mDisables.contains(key)) {
                mDisables.remove(key);
            }
        } else {
            if (!mDisables.contains(key)) {
                mDisables.add(key);
            }
        }
        //TODO: 保存这个列表
    }


    public void addNotification(int id, String tag, String packageName, int userId, int vuserId) {
        NotificationInfo notificationInfo = new NotificationInfo(id, tag, packageName, userId, vuserId);
        synchronized (mNotifications) {
            List<NotificationInfo> list = mNotifications.get(packageName);
            if (list == null) {
                list = new ArrayList<>();
                mNotifications.put(packageName, list);
            }
            if (!list.contains(notificationInfo)) {
                VLog.d(TAG, "add "+tag+" "+id);
                list.add(notificationInfo);
            }
        }
    }

    public void cancelAllNotification(Object notificationManager, String packageName, int userId, int vuserId) {
        List<NotificationInfo> infos=new ArrayList<>();
        synchronized (mNotifications) {
            List<NotificationInfo> list = mNotifications.get(packageName);
            if (list != null) {
                int count = list.size();
                for (int i = count - 1; i >= 0; i--) {
                    NotificationInfo info = list.get(i);
                    if (info.vuserId == vuserId) {
                        infos.add(info);
                        list.remove(i);
                    }
                }
            }
        }
        for(NotificationInfo info:infos){
            VLog.d(TAG, "cancel "+info.tag+" "+info.id);
            mNotificationManager.cancel(info.tag, info.id);
        }
    }


    public NotificationManager getNotificationManager() {
        return mNotificationManager;
    }

    public static VNotificationManager get() {
        return sVNotificationManager;
    }

    private static class NotificationInfo {
        int id;
        String tag;
        String packageName;
        int userId;
        int vuserId;

        public NotificationInfo(int id, String tag, String packageName, int userId, int vuserId) {
            this.id = id;
            this.tag = tag;
            this.packageName = packageName;
            this.userId = userId;
            this.vuserId = vuserId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NotificationInfo) {
                NotificationInfo that = (NotificationInfo) obj;
                return that.id == id && TextUtils.equals(that.tag, tag)
                        && TextUtils.equals(packageName, that.packageName)
                        && that.vuserId == vuserId;
            }
            return super.equals(obj);
        }
    }

}
