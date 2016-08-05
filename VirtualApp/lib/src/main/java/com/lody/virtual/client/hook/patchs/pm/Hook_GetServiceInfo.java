package com.lody.virtual.client.hook.patchs.pm;

import static android.content.pm.PackageManager.GET_DISABLED_COMPONENTS;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import android.content.ComponentName;

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
		return LocalPackageManager.getInstance().getServiceInfo(componentName, flags);
	}
}
