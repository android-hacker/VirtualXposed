package com.lody.virtual.client.hook.patchs.notification;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;
import com.lody.virtual.client.ipc.VNotificationManager;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class CancelNotificationWithTag extends Hook {

    @Override
    public String getName() {
        return "cancelNotificationWithTag";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        //15 cancelNotificationWithTag(pkg, tag, id);
        //16 cancelNotificationWithTag(pkg, tag, id);
        //17 cancelNotificationWithTag(pkg, tag, id, UserHandle.myUserId());
        //18 cancelNotificationWithTag(pkg, tag, id, UserHandle.myUserId());
        //19 cancelNotificationWithTag(pkg, tag, id, UserHandle.myUserId());
        //21 cancelNotificationWithTag(pkg, tag, id, UserHandle.myUserId());
        //22 cancelNotificationWithTag(pkg, tag, id, UserHandle.myUserId());
        //23 cancelNotificationWithTag(pkg, tag, id, UserHandle.myUserId());
        //24 cancelNotificationWithTag(pkg, tag, id, user.getIdentifier());
        //25 cancelNotificationWithTag(pkg, tag, id, user.getIdentifier());
        String pkg = HookUtils.replaceFirstAppPkg(args);
        String tag = (String) args[1];
        int id = (int) args[2];
        id = VNotificationManager.get().dealNotificationId(id, pkg, tag, getAppUserId());
        tag = VNotificationManager.get().dealNotificationTag(id, pkg, tag, getAppUserId());

        args[1] = tag;
        args[2] = id;
        VLog.d("notification", "need cancel " + tag + " " + id);
        return method.invoke(who, args);
    }
}
