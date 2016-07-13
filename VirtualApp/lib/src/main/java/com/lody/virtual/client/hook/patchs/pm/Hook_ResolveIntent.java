package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import java.lang.reflect.Method;

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
		Intent intent = (Intent) args[0];
		String resolvedType = (String) args[1];
		int flags = (int) args[2];
		ResolveInfo resolveInfo = LocalPackageManager.getInstance().resolveIntent(intent, resolvedType, flags);
		if (resolveInfo == null) {
			resolveInfo = (ResolveInfo) method.invoke(who, args);
		}
		if (resolveInfo != null && resolveInfo.activityInfo != null
				&& !getHostPkg().equals(resolveInfo.activityInfo.packageName)) {
			// 在插件中无法resolve这个Intent, 尝试在Host中resolve.
			resolveInfo = null;
		}
		return resolveInfo;
	}
}
