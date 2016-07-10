package com.lody.virtual.client.hook.patchs.media.router;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_RegisterClientAsUser extends Hook<MediaRouterServicePatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_RegisterClientAsUser(MediaRouterServicePatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "registerClientAsUser";
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
