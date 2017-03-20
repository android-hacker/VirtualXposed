package com.lody.virtual.client.hook.patchs.pm;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *         public ActivityInfo getServiceInfo(ComponentName className, int
 *         flags, int userId)
 */
/* package */ class GetReceiverInfo extends Hook {

    @Override
    public String getName() {
        return "getReceiverInfo";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        ComponentName componentName = (ComponentName) args[0];
        if (getHostPkg().equals(componentName.getPackageName())) {
            return method.invoke(who, args);
        }
        int flags = (int) args[1];
        ActivityInfo info = VPackageManager.get().getReceiverInfo(componentName, flags, 0);
        if (info == null) {
            info = (ActivityInfo) method.invoke(who, args);
            if (info == null || !isVisiblePackage(info.applicationInfo)) {
                return null;
            }
        }
        return info;
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
