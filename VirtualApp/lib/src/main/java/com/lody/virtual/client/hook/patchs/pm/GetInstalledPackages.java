package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.PackageInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
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
/* package */ class GetInstalledPackages extends Hook {

	@Override
	public String getName() {
		return "getInstalledPackages";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		int flags = (int) args[0];
		int userId = VUserHandle.myUserId();
		List<PackageInfo> packageInfos;
		if (isAppProcess()) {
			packageInfos = new ArrayList<PackageInfo>(VirtualCore.get().getInstalledAppCount());
		} else {
			packageInfos = VirtualCore.get().getUnHookPackageManager().getInstalledPackages(flags);
		}
		packageInfos.addAll(VPackageManager.get().getInstalledPackages(flags, userId));
		if (ParceledListSliceCompat.isReturnParceledListSlice(method)) {
			return ParceledListSliceCompat.create(packageInfos);
		} else {
			return packageInfos;
		}
	}
}
