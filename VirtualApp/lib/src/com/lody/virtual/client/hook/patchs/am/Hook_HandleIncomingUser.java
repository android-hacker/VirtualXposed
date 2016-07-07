package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_HandleIncomingUser extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_HandleIncomingUser(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "handleIncomingUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int lastIndex = args.length - 1;
		if (args[lastIndex] instanceof String) {
			String pkgName = (String) args[lastIndex];
			if (isAppPkg(pkgName)) {
				args[lastIndex] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
