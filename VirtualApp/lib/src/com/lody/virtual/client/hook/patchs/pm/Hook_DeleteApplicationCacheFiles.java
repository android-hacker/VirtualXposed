package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_DeleteApplicationCacheFiles extends Hook<PackageManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_DeleteApplicationCacheFiles(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "deleteApplicationCacheFiles";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		// TODO
		return method.invoke(who, args);
	}
}
