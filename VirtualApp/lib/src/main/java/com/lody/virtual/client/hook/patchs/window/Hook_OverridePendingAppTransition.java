package com.lody.virtual.client.hook.patchs.window;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_OverridePendingAppTransition extends BaseHook_PatchSession {

	@Override
	public String getName() {
		return "overridePendingAppTransition";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (isAppPkg(pkgName)) {
			args[0] = getHostPkg();
		}
		return super.onHook(who, method, args);
	}
}
