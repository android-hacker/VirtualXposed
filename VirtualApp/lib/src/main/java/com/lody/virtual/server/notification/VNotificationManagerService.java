package com.lody.virtual.server.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;

import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.server.INotificationManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class VNotificationManagerService extends INotificationManager.Stub {
    private static final AtomicReference<VNotificationManagerService> gService = new AtomicReference<>();
    private NotificationManager mNotificationManager;
    static final String TAG = NotificationCompat.class.getSimpleName();
    private final List<String> mDisables = new ArrayList<>();
    //需要保存
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
     * 处理通知栏id
     *
     * @param id
     * @param packageName
     * @return
     */
    @Override
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
    @Override
    public String dealNotificationTag(int id, String packageName, String tag, int userId) {
        //最好是知道vuserid
        if (TextUtils.equals(mContext.getPackageName(), packageName)) {
            return tag;
        }
        if (tag == null) {
            return packageName + "@" + userId;
        }
        return packageName + ":" + tag + "@" + userId;
    }

    @Override
    public boolean areNotificationsEnabledForPackage(String packageName, int vuserId) {
        //最好是知道vuserid
        return !mDisables.contains(packageName + ":" + vuserId);
    }

    @Override
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

    @Override
    public void addNotification(int id, String tag, String packageName, int vuserId) {
        NotificationInfo notificationInfo = new NotificationInfo(id, tag, packageName, vuserId);
        synchronized (mNotifications) {
            List<NotificationInfo> list = mNotifications.get(packageName);
            if (list == null) {
                list = new ArrayList<>();
                mNotifications.put(packageName, list);
            }
            if (!list.contains(notificationInfo)) {
                VLog.d(TAG, "add " + tag + " " + id);
                list.add(notificationInfo);
            }
        }
    }

    @Override
    public void cancelAllNotification(String packageName, int vuserId) {
        List<NotificationInfo> infos = new ArrayList<>();
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
        for (NotificationInfo info : infos) {
            VLog.d(TAG, "cancel " + info.tag + " " + info.id);
            mNotificationManager.cancel(info.tag, info.id);
        }
    }

    private static class NotificationInfo {
        int id;
        String tag;
        String packageName;
        int vuserId;

        NotificationInfo(int id, String tag, String packageName, int vuserId) {
            this.id = id;
            this.tag = tag;
            this.packageName = packageName;
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
