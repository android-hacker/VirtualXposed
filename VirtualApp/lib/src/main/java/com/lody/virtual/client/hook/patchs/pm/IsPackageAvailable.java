package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class IsPackageAvailable extends Hook {

	@Override
	public String getName() {
		return "isPackageAvailable";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (isAppPkg(pkgName)) {
			return true;
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
