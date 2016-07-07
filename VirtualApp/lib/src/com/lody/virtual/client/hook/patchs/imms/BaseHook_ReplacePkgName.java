package com.lody.virtual.client.hook.patchs.imms;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ abstract class BaseHook_ReplacePkgName extends Hook<MmsPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public BaseHook_ReplacePkgName(MmsPatch patchObject) {
		super(patchObject);
	}

	public int getIndex() {
		return 0;
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		return method.invoke(who, args);
	}
}
