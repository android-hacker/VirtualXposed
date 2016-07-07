package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.hook.base.Hook;

import android.content.Intent;

/**
 * @author Lody
 *
 *
 *         原型: public ResolveInfo resolveService(Intent intent, String
 *         resolvedType, int flags, int userId)
 */
/* package */ class Hook_ResolveService extends Hook<PackageManagerPatch> {

	private int intentIndex = -1;
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_ResolveService(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "resolveService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		return LocalPackageManager.getInstance().resolveService((Intent) args[0], // intent
				(String) args[1], // resolvedType
				(Integer) args[2]// flags
		);
	}
}
