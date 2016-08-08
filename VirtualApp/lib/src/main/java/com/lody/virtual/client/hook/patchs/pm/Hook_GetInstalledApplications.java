package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;
import java.util.List;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetInstalledApplications extends Hook {

	@Override
	public String getName() {
		return "getInstalledApplications";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		int flags = (Integer) args[0];
		List<ApplicationInfo> appInfos = VPackageManager.getInstance().getInstalledApplications(flags);
		if (isMainProcess()) {
			PackageManager hostPM = getUnhookPM();
			// noinspection WrongConstant
			appInfos.addAll(hostPM.getInstalledApplications(flags));
		}
		if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
			return ParceledListSliceCompat.create(appInfos);
		}
		return appInfos;
	}
}
