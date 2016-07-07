package com.lody.virtual.client.hook.patchs.graphics;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_RequestBufferForProcess extends Hook<GraphicsStatsPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_RequestBufferForProcess(GraphicsStatsPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "requestBufferForProcess";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (isAppPkg(pkgName)) {
			args[0] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
