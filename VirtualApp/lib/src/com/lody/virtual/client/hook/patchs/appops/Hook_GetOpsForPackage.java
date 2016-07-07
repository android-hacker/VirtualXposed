package com.lody.virtual.client.hook.patchs.appops;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetOpsForPackage extends Hook<AppOpsManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetOpsForPackage(AppOpsManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getOpsForPackage";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		String pkgName = (String) args[1];
		if (isAppPkg(pkgName)) {
			args[1] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
