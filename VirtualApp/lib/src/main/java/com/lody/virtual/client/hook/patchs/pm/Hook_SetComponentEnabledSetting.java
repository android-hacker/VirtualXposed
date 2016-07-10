package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.content.ComponentName;

/**
 * @author Lody
 *
 */
/* package */ class Hook_SetComponentEnabledSetting extends Hook<PackageManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_SetComponentEnabledSetting(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "setComponentEnabledSetting";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName componentName = (ComponentName) args[0];
		if (componentName != null) {
			String pkgName = componentName.getPackageName();
			if (pkgName != null) {
				if (isAppPkg(pkgName)) {
					return 0;
				}
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
