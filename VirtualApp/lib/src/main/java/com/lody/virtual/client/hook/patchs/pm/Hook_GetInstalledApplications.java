package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
import java.util.List;

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
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 1 && args[1] instanceof Integer) {
			userId = (int) args[1];
		}
		List<ApplicationInfo> appInfos = VPackageManager.getInstance().getInstalledApplications(flags, userId);
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
