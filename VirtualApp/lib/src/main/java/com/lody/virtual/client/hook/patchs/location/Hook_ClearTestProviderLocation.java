package com.lody.virtual.client.hook.patchs.location;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_ClearTestProviderLocation extends Hook {

	@Override
	public String getName() {
		return "clearTestProviderLocation";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args[1] instanceof String) {
			String pkgName = (String) args[1];
			if (isAppPkg(pkgName)) {
				args[1] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
