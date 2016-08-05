package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.proto.AppInfo;

import android.os.Process;

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
		List<AppInfo> appInfos = VirtualCore.getCore().getAllApps();
		List<String> appList = new ArrayList<>(appInfos.size());
		for (AppInfo appInfo : appInfos) {
			appList.add(appInfo.packageName);
		}
		return appList.toArray(new String[appInfos.size()]);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
