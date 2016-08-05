package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetInstallerPackageName extends Hook {

	@Override
	public String getName() {
		return "getInstallerPackageName";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (isAppPkg(pkgName)) {
			// 插件的Installer为宿主
			return getHostPkg();
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
