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
            int rs = NotificationHandler.getInstance().dealNotification(getHostContext(), pkgName, args);
            if (rs < 0) {
                return 0;
            }
            //在miui没用
//            if(rs > 0){
//                //系统样式
//                //先显示，再修改icon
//                method.invoke(who, args);
//                NotificationHandler.getInstance().dealNotificationIcon(rs, pkgName, args);
//                //修改通知栏
//                return method.invoke(who, args);
//            }
            //
        }
        return method.invoke(who, args);
    }
}
