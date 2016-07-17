package com.lody.virtual.client.hook.patchs.notification;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.patchs.notification.compat.NotificationHandler;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class Hook_EnqueueNotification extends Hook<NotificationManagerPatch> {
    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_EnqueueNotification(NotificationManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "enqueueNotification";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        String pkgName = (String) args[0];
        if (!VirtualCore.getCore().isHostPackageName(pkgName)) {
            args[0] = VirtualCore.getCore().getContext().getPackageName();
//			return 0;
            if (!NotificationHandler.getInstance().dealNotification(getHostContext(), pkgName, args)) {
                return 0;
            }
        }
        return method.invoke(who, args);
    }
}
