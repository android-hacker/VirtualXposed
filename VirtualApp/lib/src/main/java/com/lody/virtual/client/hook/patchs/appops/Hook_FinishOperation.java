package com.lody.virtual.client.hook.patchs.appops;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_FinishOperation extends Hook {

	@Override
	public String getName() {
		return "finishOperation";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		String pkgName = (String) args[3];
		if (isAppPkg(pkgName)) {
			args[3] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
