package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class IsPackageForzen extends Hook {

	@Override
	public String getName() {
		return "sPackageForzen";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
//		String pkgName = (String) args[0];
		return false;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
