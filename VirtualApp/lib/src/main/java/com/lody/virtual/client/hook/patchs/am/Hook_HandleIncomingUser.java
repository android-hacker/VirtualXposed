package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_HandleIncomingUser extends Hook {

	@Override
	public String getName() {
		return "handleIncomingUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int lastIndex = args.length - 1;
		if (args[lastIndex] instanceof String) {
			String pkgName = (String) args[lastIndex];
			if (isAppPkg(pkgName)) {
				args[lastIndex] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
