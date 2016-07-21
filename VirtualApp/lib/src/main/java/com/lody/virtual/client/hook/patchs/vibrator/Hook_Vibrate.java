package com.lody.virtual.client.hook.patchs.vibrator;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_Vibrate extends Hook {

	@Override
	public String getName() {
		return "vibrate";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		String opPkg = (String) args[1];
		if (isAppPkg(opPkg)) {
			args[1] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
