package com.lody.virtual.client.hook.patchs.pm;

import android.content.ComponentName;
import android.content.pm.ProviderInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *         <p>
 *         <p>
 *         原型: public ActivityInfo getServiceInfo(ComponentName className, int
 *         flags, int userId)
 */
/* package */ class GetProviderInfo extends Hook {

    @Override
    public String getName() {
        return "getProviderInfo";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        ComponentName componentName = (ComponentName) args[0];
        int flags = (int) args[1];
        if (getHostPkg().equals(componentName.getPackageName())) {
            return method.invoke(who, args);
        }
        int userId = VUserHandle.myUserId();
        ProviderInfo info = VPackageManager.get().getProviderInfo(componentName, flags, userId);
        if (info == null) {
            info = (ProviderInfo) method.invoke(who, args);
            if (info == null || !isVisiblePackage(info.applicationInfo)) {
                return null;
            }
        }
        return info;
    }

}
