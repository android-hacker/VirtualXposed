package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager#checkPermission(String, String, int)
 */
/* package */ class Hook_CheckPermission extends Hook<PackageManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_CheckPermission(PackageManagerPatch patchObject) {
		super(patchObject);
	}

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
