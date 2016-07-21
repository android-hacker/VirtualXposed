package com.lody.virtual.client.hook.patchs.location;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_SetTestProviderLocation extends Hook {

	@Override
	public String getName() {
		return "setTestProviderLocation";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args[2] instanceof String) {
			String pkgName = (String) args[2];
			if (isAppPkg(pkgName)) {
				args[2] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
