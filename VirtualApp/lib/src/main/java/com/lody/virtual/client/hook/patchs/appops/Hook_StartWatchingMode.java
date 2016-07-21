package com.lody.virtual.client.hook.patchs.appops;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_StartWatchingMode extends Hook {

	@Override
	public String getName() {
		return "startWatchingMode";
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
