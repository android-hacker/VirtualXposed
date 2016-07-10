package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.content.ComponentName;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetComponentEnabledSetting extends Hook<PackageManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetComponentEnabledSetting(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getComponentEnabledSetting";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		// NOTE: 有4个状态: 0默认 1可用 2禁止 3User Disable
		ComponentName component = (ComponentName) args[0];
		if (component != null) {
			String pkgName = component.getPackageName();
			if (isAppPkg(pkgName)) {
				return 1;
			}
		}
		return method.invoke(who, args);
	}
}
