package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.compat.ParceledListSliceCompat;
import com.lody.virtual.helper.proto.VParceledListSlice;

import android.content.pm.PackageInfo;

/**
 * @author Lody
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
		List<PackageInfo> packageInfos;
		if (isAppProcess()) {
			packageInfos = new ArrayList<PackageInfo>(VirtualCore.getCore().getAppCount());
		} else {
			packageInfos = VirtualCore.getCore().getUnHookPackageManager().getInstalledPackages(flags);
		}
		VParceledListSlice<PackageInfo> listSlice = VPackageManager.getInstance().getInstalledPackages(flags);
		packageInfos.addAll(listSlice.getList());
		if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
			return ParceledListSliceCompat.create(packageInfos);
		} else {
			return packageInfos;
		}
	}
}
