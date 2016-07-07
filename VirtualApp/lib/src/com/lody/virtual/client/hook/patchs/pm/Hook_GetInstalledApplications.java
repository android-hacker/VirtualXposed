package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;
import java.util.List;

import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetInstalledApplications extends Hook<PackageManagerPatch> {

	private final char[] mLock = new char[0];

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetInstalledApplications(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getInstalledApplications";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		int flags = (Integer) args[0];
		List<ApplicationInfo> appInfos = LocalPackageManager.getInstance().getInstalledApplications(flags);
		if (isMainProcess()) {
			PackageManager hostPM = getUnhookPM();
			appInfos.addAll(hostPM.getInstalledApplications(flags));
		}
		if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
			return ParceledListSliceCompat.create(appInfos);
		}
		return appInfos;
	}
}
