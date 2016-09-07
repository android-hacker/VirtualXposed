package com.lody.virtual.client.hook.patchs.window;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class OverridePendingAppTransition extends BasePatchSession {

	@Override
	public String getName() {
		return "overridePendingAppTransition";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		if (args[0] instanceof String) {
			args[0] = getHostPkg();
		}
		return super.call(who, method, args);
	}
}
