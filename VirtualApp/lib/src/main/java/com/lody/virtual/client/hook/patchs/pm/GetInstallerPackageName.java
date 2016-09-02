package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class GetInstallerPackageName extends Hook {

	@Override
	public String getName() {
		return "getInstallerPackageName";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return "com.android.vending";
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
