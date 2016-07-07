package com.lody.virtual.client.hook.patchs.vibrator;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_Vibrate extends Hook<VibratorPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_Vibrate(VibratorPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "vibrate";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		String opPkg = (String) args[1];
		if (isAppPkg(opPkg)) {
			args[1] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
