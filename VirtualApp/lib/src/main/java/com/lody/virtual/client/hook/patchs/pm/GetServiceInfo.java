package com.lody.virtual.client.hook.patchs.pm;

import android.content.ComponentName;
import android.content.pm.ServiceInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

import static android.content.pm.PackageManager.GET_DISABLED_COMPONENTS;

/**
 * @author Lody
 *         <p>
 *         public ActivityInfo getServiceInfo(ComponentName className, int
 *         flags, int userId)
 */
/* package */ class GetServiceInfo extends Hook {

    @Override
    public String getName() {
        return "getServiceInfo";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        ComponentName componentName = (ComponentName) args[0];
        int flags = (int) args[1];
        if ((flags & GET_DISABLED_COMPONENTS) == 0) {
            flags |= GET_DISABLED_COMPONENTS;
        }
        int userId = VUserHandle.myUserId();
        ServiceInfo info = VPackageManager.get().getServiceInfo(componentName, flags, userId);
        if (info != null) {
            return info;
        }
        info = (ServiceInfo) method.invoke(who, args);
        if (info != null) {
            if (getHostPkg().equals(info.packageName) || ComponentUtils.isSystemApp(info.applicationInfo)) {
                return info;
            }
        }
        return null;
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
