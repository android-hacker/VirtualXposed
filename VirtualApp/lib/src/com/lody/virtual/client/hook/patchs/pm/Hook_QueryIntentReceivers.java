package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.hook.base.Hook;

import android.content.Intent;

/**
 * @author Lody
 *
 */
/* package */ class Hook_QueryIntentReceivers extends Hook<PackageManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_QueryIntentReceivers(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "queryIntentReceivers";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return LocalPackageManager.getInstance().queryIntentReceivers((Intent) args[0], (String) args[1],
				(Integer) args[2]);
	}
}
