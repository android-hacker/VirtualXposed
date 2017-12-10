package com.lody.virtual.server.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;

import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.server.interfaces.INotificationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class VNotificationManagerService implements INotificationManager {
    private static final AtomicReference<VNotificationManagerService> gService = new AtomicReference<>();
    private NotificationManager mNotificationManager;
    static final String TAG = NotificationCompat.class.getSimpleName();
    private final List<String> mDisables = new ArrayList<>();
    //VApp's Notifications
    private final HashMap<String, List<NotificationInfo>> mNotifications = new HashMap<>();
    private Context mContext;

    private VNotificationManagerService(Context context) {
        mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static void systemReady(Context context) {
        VNotificationManagerService instance = new VNotificationManagerService(context);
        gService.set(instance);
    }

    public static VNotificationManagerService get() {
        return gService.get();
    }

    /***
     * fake notification's id
     *
     * @param id          notification's id
     * @param packageName notification's package
     * @param userId      user
     * @return
     */
    @Override
    public int dealNotificationId(int id, String packageName, String tag, int userId) {
        return id;
    }

    /***
     * fake notification's tag
     *
     * @param id          notification's id
     * @param packageName notification's package
     * @param tag         notification's tag
     * @param userId      user
     * @return
     */
    @Override
    public String dealNotificationTag(int id, String packageName, String tag, int userId) {
        if (TextUtils.equals(mContext.getPackageName(), packageName)) {
            return tag;
        }
        if (tag == null) {
            return packageName + "@" + userId;
        }
        return packageName + ":" + tag + "@" + userId;
    }

    @Override
    public boolean areNotificationsEnabledForPackage(String packageName, int userId) {
        return !mDisables.contains(packageName + ":" + userId);
    }

    @Override
    public void setNotificationsEnabledForPackage(String packageName, boolean enable, int userId) {
        String key = packageName + ":" + userId;
        if (enable) {
            if (mDisables.contains(key)) {
                mDisables.remove(key);
            }
        } else {
            if (!mDisables.contains(key)) {
                mDisables.add(key);
            }
        }
        //TODO: save mDisables ?
    }

    @Override
    public void addNotification(int id, String tag, String packageName, int userId) {
        NotificationInfo notificationInfo = new NotificationInfo(id, tag, packageName, userId);
        synchronized (mNotifications) {
            List<NotificationInfo> list = mNotifications.get(packageName);
            if (list == null) {
                list = new ArrayList<>();
                mNotifications.put(packageName, list);
            }
            if (!list.contains(notificationInfo)) {
                list.add(notificationInfo);
            }
        }
    }

    @Override
    public void cancelAllNotification(String packageName, int userId) {
        List<NotificationInfo> infos = new ArrayList<>();
        synchronized (mNotifications) {
            List<NotificationInfo> list = mNotifications.get(packageName);
            if (list != null) {
                int count = list.size();
                for (int i = count - 1; i >= 0; i--) {
                    NotificationInfo info = list.get(i);
                    if (info.userId == userId) {
                        infos.add(info);
                        list.remove(i);
                    }
                }
            }
        }
        for (NotificationInfo info : infos) {
            VLog.d(TAG, "cancel " + info.tag + " " + info.id);
            mNotificationManager.cancel(info.tag, info.id);
        }
    }

    private static class NotificationInfo {
        int id;
        String tag;
        String packageName;
        int userId;

        NotificationInfo(int id, String tag, String packageName, int userId) {
            this.id = id;
            this.tag = tag;
            this.packageName = packageName;
            this.userId = userId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof NotificationInfo) {
                NotificationInfo that = (NotificationInfo) obj;
                return that.id == id && TextUtils.equals(that.tag, tag)
                        && TextUtils.equals(packageName, that.packageName)
                        && that.userId == userId;
            }
            return super.equals(obj);
        }
    }

}
