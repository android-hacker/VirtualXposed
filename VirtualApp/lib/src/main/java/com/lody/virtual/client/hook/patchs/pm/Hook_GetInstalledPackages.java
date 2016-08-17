package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.PackageInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#getInstalledPackages(int, int)
 *
 */
@SuppressWarnings({"unchecked", "WrongConstant"})
/* package */ class Hook_GetInstalledPackages extends Hook {

	@Override
	public String getName() {
		return "getInstalledPackages";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int flags = (int) args[0];
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 1 && args[1] instanceof Integer) {
			userId = (int) args[1];
		}
		List<PackageInfo> packageInfos;
		if (isAppProcess()) {
			packageInfos = new ArrayList<PackageInfo>(VirtualCore.getCore().getAppCount());
		} else {
			packageInfos = VirtualCore.getCore().getUnHookPackageManager().getInstalledPackages(flags);
		}
		packageInfos.addAll(VPackageManager.getInstance().getInstalledPackages(flags, userId));
		if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
			return ParceledListSliceCompat.create(packageInfos);
		} else {
			return packageInfos;
		}
	}
}
