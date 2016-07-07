package com.lody.virtual.client.hook.patchs.backup;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_BeginRestoreSession extends Hook<BackupManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_BeginRestoreSession(BackupManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "beginRestoreSession";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (isAppPkg(pkgName)) {
			return null;
		}
		return method.invoke(who, args);
	}
}
