package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.ApplicationInfo;

import com.lody.virtual.client.env.BlackList;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetApplicationInfo extends Hook<PackageManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetApplicationInfo(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getApplicationInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkg = (String) args[0];
		int flags = (int) args[1];
		if (getHostPkg().equals(pkg)) {
			return method.invoke(who, args);
		}
		if (BlackList.isBlackPkg(pkg)) {
			// 隔离Gms
			return null;
		}
		ApplicationInfo applicationInfo = (ApplicationInfo) method.invoke(who, args);
		if (applicationInfo != null) {
			return applicationInfo;
		}
		return LocalPackageManager.getInstance().getApplicationInfo(pkg, flags);
	}
}
