package com.lody.virtual.client.hook.patchs.pm;

import android.os.Binder;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Lody
 *
 */
/* package */ class GetPackagesForUid extends Hook {


	@Override
	public String getName() {
		return "getPackagesForUid";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		int uid = (int) args[0];
		int callingUid = Binder.getCallingUid();
		if (uid == VirtualCore.get().myUid()) {
			uid = getBaseVUid();
		}
		String[] callingPkgs = VPackageManager.get().getPackagesForUid(callingUid);
		String[] pkgs = VPackageManager.get().getPackagesForUid(uid);

		ArrayList<String> pkgList = new ArrayList<>(2);
		if (callingPkgs != null && callingPkgs.length > 0) {
			pkgList.addAll(Arrays.asList(callingPkgs));
		}
		if (pkgs != null && pkgs.length > 0) {
			pkgList.addAll(Arrays.asList(pkgs));
		}
		return pkgList.toArray(new String[pkgList.size()]);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
