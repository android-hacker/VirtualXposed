package com.lody.virtual.client.hook.patchs.notification;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;
import com.lody.virtual.client.ipc.VNotificationManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class CancelAllNotifications extends Hook {

    @Override
    public String getName() {
        return "cancelAllNotifications";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        String pkg = HookUtils.replaceFirstAppPkg(args);
//        int user = 0;
//        if (Build.VERSION.SDK_INT >= 17) {
//            user = (int) args[1];
//        }
        if (VirtualCore.get().isAppInstalled(pkg)) {
            VNotificationManager.get().cancelAllNotification(pkg, getAppUserId());
            return 0;
        }
        return method.invoke(who, args);
    }
}
