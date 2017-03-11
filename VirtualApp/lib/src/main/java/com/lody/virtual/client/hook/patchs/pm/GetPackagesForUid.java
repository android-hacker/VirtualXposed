package com.lody.virtual.client.hook.patchs.pm;

import android.os.Binder;
import android.os.Process;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.helper.collection.ArraySet;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

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
		String[] targetPkgs = VPackageManager.get().getPackagesForUid(uid);
		String[] selfPkgs = VPackageManager.get().getPackagesForUid(Process.myUid());

		Set<String> pkgList = new ArraySet<>(2);
		if (callingPkgs != null && callingPkgs.length > 0) {
			pkgList.addAll(Arrays.asList(callingPkgs));
		}
		if (targetPkgs != null && targetPkgs.length > 0) {
			pkgList.addAll(Arrays.asList(targetPkgs));
		}
		if (selfPkgs != null && selfPkgs.length > 0) {
			pkgList.addAll(Arrays.asList(selfPkgs));
		}
		return pkgList.toArray(new String[pkgList.size()]);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
