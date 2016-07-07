package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.hook.base.Hook;

import android.content.Intent;

/**
 * @author Lody
 *
 *         <p/>
 *         Android 4.4+
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_QueryContentProviders extends Hook<PackageManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_QueryContentProviders(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "queryContentProviders";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return LocalPackageManager.getInstance().queryIntentContentProviders((Intent) args[0], (String) args[1],
				(Integer) args[2]);
	}

}
