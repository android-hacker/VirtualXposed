package com.lody.virtual.client.hook.patchs.location;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_SetTestProviderStatus extends Hook<LocationManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_SetTestProviderStatus(LocationManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "setTestProviderStatus";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[4];
		if (isAppPkg(pkgName)) {
			args[4] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
