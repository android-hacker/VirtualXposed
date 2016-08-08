package com.lody.virtual.client.hook.patchs.appops;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_SetMode extends Hook {

	@Override
	public String getName() {
		return "setMode";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		String pkgName = (String) args[2];
		if (isAppPkg(pkgName)) {
			args[2] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
