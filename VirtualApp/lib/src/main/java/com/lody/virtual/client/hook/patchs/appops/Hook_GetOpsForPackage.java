package com.lody.virtual.client.hook.patchs.appops;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetOpsForPackage extends Hook {

	@Override
	public String getName() {
		return "getOpsForPackage";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		String pkgName = (String) args[1];
		if (isAppPkg(pkgName)) {
			args[1] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
