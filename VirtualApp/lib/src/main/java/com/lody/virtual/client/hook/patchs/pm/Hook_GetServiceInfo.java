package com.lody.virtual.client.hook.patchs.pm;

import android.content.ComponentName;
import android.content.pm.ServiceInfo;

import com.lody.virtual.client.fixer.ComponentFixer;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import java.lang.reflect.Method;

import static android.content.pm.PackageManager.GET_DISABLED_COMPONENTS;

/**
 * @author Lody
 *
 *
 *         原型: public ActivityInfo getServiceInfo(ComponentName className, int
 *         flags, int userId)
 *
 */
/* package */ class Hook_GetServiceInfo extends Hook {

	@Override
	public String getName() {
		return "getServiceInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName componentName = (ComponentName) args[0];
		int flags = (int) args[1];
		if ((flags & GET_DISABLED_COMPONENTS) == 0) {
			flags |= GET_DISABLED_COMPONENTS;
		}
		ServiceInfo serviceInfo = LocalPackageManager.getInstance().getServiceInfo(componentName, flags);
		if (serviceInfo != null) {
			ComponentFixer.fixUid(serviceInfo.applicationInfo);
		}
		return serviceInfo;
	}
}
