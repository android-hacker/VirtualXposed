package com.lody.virtual.client.hook.patchs.location;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_ClearTestProviderLocation extends Hook<LocationManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_ClearTestProviderLocation(LocationManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "clearTestProviderLocation";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args[1] instanceof String) {
			String pkgName = (String) args[1];
			if (isAppPkg(pkgName)) {
				args[1] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
