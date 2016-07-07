package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.hook.base.Hook;

import android.content.Intent;

/**
 * @author Lody
 *
 *
 *         原型: resolveIntent(Intent intent, String resolvedType, int flags, int
 *         userId)
 */
/* package */ class Hook_ResolveIntent extends Hook<PackageManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_ResolveIntent(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "resolveIntent";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return LocalPackageManager.getInstance().resolveIntent((Intent) args[0], // intent
				(String) args[1], // resolvedType
				(Integer) args[2]// flags
		);
	}
}
