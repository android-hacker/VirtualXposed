package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;
import java.util.List;

import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;

import android.content.pm.PackageInfo;

/**
 * @author Lody
 *
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_GetInstalledPackages extends Hook<PackageManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetInstalledPackages(PackageManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getInstalledPackages";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (true) {
			return method.invoke(who, args);
		}
		int flags = (int) args[0];
		List<PackageInfo> pkgInfos = LocalPackageManager.getInstance().getInstalledPackages(flags);
		if (isMainProcess()) {
			// noinspection WrongConstant
			pkgInfos.addAll(getUnhookPM().getInstalledPackages(flags));
		}
		if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
			return ParceledListSliceCompat.create(pkgInfos);
		}
		return pkgInfos;
	}
}
