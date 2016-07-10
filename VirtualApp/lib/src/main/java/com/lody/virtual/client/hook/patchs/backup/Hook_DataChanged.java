package com.lody.virtual.client.hook.patchs.backup;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_DataChanged extends Hook<BackupManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_DataChanged(BackupManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "dataChanged";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (isAppPkg(pkgName)) {
			// Nothing to do
			return 0;
		}
		return method.invoke(who, args);
	}
}
