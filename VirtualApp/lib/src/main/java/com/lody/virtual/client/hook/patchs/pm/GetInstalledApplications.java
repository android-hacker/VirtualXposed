package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.ApplicationInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Lody
 *
 */
/* package */ class GetInstalledApplications extends Hook {

	@Override
	public String getName() {
		return "getInstalledApplications";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {

		int flags = (Integer) args[0];
		int userId = VUserHandle.myUserId();
		List<ApplicationInfo> appInfos = VPackageManager.get().getInstalledApplications(flags, userId);
		if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
			return ParceledListSliceCompat.create(appInfos);
		}
		return appInfos;
	}
}
