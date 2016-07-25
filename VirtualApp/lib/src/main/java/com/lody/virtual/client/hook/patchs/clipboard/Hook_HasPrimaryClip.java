package com.lody.virtual.client.hook.patchs.clipboard;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_HasPrimaryClip extends Hook {

	@Override
	public String getName() {
		return "hasPrimaryClip";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args.length > 0 && args[0] instanceof String) {
			String callingPackage = (String) args[0];
			if (isAppPkg(callingPackage)) {
				args[0] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
