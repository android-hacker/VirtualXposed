package com.lody.virtual.client.hook.patchs.appops;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_NoteOperation extends Hook<AppOpsManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_NoteOperation(AppOpsManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "noteOperation";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		String pkgName = (String) args[2];
		if (isAppPkg(pkgName)) {
			args[2] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
