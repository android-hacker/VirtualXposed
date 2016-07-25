package com.lody.virtual.client.hook.patchs.clipboard;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_SetPrimaryClip extends Hook {

	@Override
	public String getName() {
		return "setPrimaryClip";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args.length > 1 && args[1] instanceof String) {
			String callingPackage = (String) args[1];
			if (isAppPkg(callingPackage)) {
				args[1] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
