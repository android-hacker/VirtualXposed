package com.lody.virtual.client.hook.patchs.clipboard;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetPrimaryClip extends Hook<ClipBoardPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetPrimaryClip(ClipBoardPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getPrimaryClip";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args.length > 0 && args[0] instanceof String) {
			String callingPackage = (String) args[0];
			if (isAppPkg(callingPackage)) {
				args[0] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
