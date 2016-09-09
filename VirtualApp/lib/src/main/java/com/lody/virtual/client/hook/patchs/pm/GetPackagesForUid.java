package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;

import java.lang.reflect.Method;

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
		if (uid == VirtualCore.get().myUid()) {
			uid = getBaseVUid();
		}
		String[] pkgList = VPackageManager.get().getPackagesForUid(uid);
		if (pkgList == null) {
			pkgList = new String[0];
		}
		return pkgList;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
