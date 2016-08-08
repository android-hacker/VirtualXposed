package com.lody.virtual.client.hook.patchs.pm;

import android.os.Process;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.proto.AppInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager#getPackagesForUid(int)
 */
/* package */ class Hook_GetPackagesForUid extends Hook {

	@Override
	public String getName() {
		return "getPackagesForUid";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		int uid = (int) args[0];
		return uid == Process.myUid();
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		VClientImpl client = VClientImpl.getClient();
		List<String> sharedPackages = client.getSharedPackages();
		List<String> packages = new ArrayList<>(sharedPackages.size() + 1);
		String initialPackage = client.geInitialPackage();
		packages.add(initialPackage);
		packages.addAll(sharedPackages);
		List<AppInfo> appInfos = VirtualCore.getCore().getAllApps();
		for (AppInfo appInfo : appInfos) {
			packages.add(appInfo.packageName);
		}
		return packages.toArray(new String[packages.size()]);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
