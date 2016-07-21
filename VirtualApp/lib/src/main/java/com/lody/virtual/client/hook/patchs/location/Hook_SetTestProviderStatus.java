package com.lody.virtual.client.hook.patchs.location;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_SetTestProviderStatus extends Hook {

	@Override
	public String getName() {
		return "setTestProviderStatus";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[4];
		if (isAppPkg(pkgName)) {
			args[4] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
