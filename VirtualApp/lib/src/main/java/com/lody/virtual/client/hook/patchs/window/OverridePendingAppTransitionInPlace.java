package com.lody.virtual.client.hook.patchs.window;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class OverridePendingAppTransitionInPlace extends Hook {

	@Override
	public String getName() {
		return "overridePendingAppTransitionInPlace";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		if (args[0] instanceof String) {
			args[0] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
