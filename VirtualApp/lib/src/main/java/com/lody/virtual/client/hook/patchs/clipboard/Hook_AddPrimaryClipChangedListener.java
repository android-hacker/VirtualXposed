package com.lody.virtual.client.hook.patchs.clipboard;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_AddPrimaryClipChangedListener extends Hook {

	@Override
	public String getName() {
		return "addPrimaryClipChangedListener";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args.length > 2 && args[2] instanceof String) {
			String callingPackage = (String) args[2];
			if (isAppPkg(callingPackage)) {
				args[1] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
