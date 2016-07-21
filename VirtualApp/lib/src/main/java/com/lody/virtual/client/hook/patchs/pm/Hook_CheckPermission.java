package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager#checkPermission(String, String, int)
 */
/* package */ class Hook_CheckPermission extends Hook {

	@Override
	public String getName() {
		return "checkPermission";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args.length > 1 && args[1] instanceof String) {
			String pkg = (String) args[1];
			if (isAppPkg(pkg)) {
				args[1] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
