package com.lody.virtual.client.hook.patchs.notification;

import android.app.Notification;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VNotificationManager;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *         <p>
 *         >=4.0.3
 */
/* package */ class EnqueueNotificationWithTag extends Hook {

    @Override
    public String getName() {
        return "enqueueNotificationWithTag";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        //15 enqueueNotificationWithTag(pkg, tag, id, notification, idOut);
        //16 enqueueNotificationWithTag(pkg, tag, id, notification, idOut);
        //17 enqueueNotificationWithTag(pkg, tag, id, notification, idOut, UserHandle.myUserId());
        //18 enqueueNotificationWithTag(pkg, mContext.getBasePackageName(), tag, id, notification, idOut, UserHandle.myUserId());
        //19 enqueueNotificationWithTag(pkg, mContext.getOpPackageName(), tag, id, notification, idOut, UserHandle.myUserId());
        //21 enqueueNotificationWithTag(pkg, mContext.getOpPackageName(), tag, id, notification, idOut, UserHandle.myUserId());
        //22 enqueueNotificationWithTag(pkg, mContext.getOpPackageName(), tag, id, notification, idOut, UserHandle.myUserId());
        //23 enqueueNotificationWithTag(pkg, mContext.getOpPackageName(), tag, id, notification, idOut, UserHandle.myUserId());
        //24 enqueueNotificationWithTag(pkg, mContext.getOpPackageName(), tag, id, notification, idOut, user.getIdentifier());
        //25 enqueueNotificationWithTag(pkg, mContext.getOpPackageName(), tag, id, notification, idOut, user.getIdentifier());
        String pkg = (String) args[0];
        int notificationIndex = ArrayUtils.indexOfFirst(args, Notification.class);
        int idIndex = ArrayUtils.indexOfFirst(args, Integer.class);
        int tagIndex = (Build.VERSION.SDK_INT >= 18 ? 2 : 1);
        int id = (int) args[idIndex];
//        int user = (Build.VERSION.SDK_INT>=17?((int)args[args.length-1]):0);
        String tag = (String) args[tagIndex];

        id = VNotificationManager.get().dealNotificationId(id, pkg, tag, getAppUserId());
        tag= VNotificationManager.get().dealNotificationTag(id, pkg, tag, getAppUserId());
        args[idIndex] = id;
        args[tagIndex] = tag;
        //key(tag,id)
        Notification notification = (Notification) args[notificationIndex];
        if (!VNotificationManager.get().dealNotification(id, notification, pkg)) {
            return 0;
        }
        VNotificationManager.get().addNotification(id, tag, pkg, getAppUserId());
        args[0] = getHostPkg();
        if (Build.VERSION.SDK_INT >= 18 && args[1] instanceof String) {
            args[1] = getHostPkg();
        }
        return method.invoke(who, args);
    }
}
