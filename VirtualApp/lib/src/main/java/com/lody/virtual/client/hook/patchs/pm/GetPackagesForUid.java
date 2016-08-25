package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.proto.AppSetting;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager#getPackagesForUid(int)
 */
/* package */ class GetPackagesForUid extends Hook {


	@Override
	public String getName() {
		return "getPackagesForUid";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		List<AppSetting> settings = VirtualCore.get().getAllApps();
		List<String> pkgList = new ArrayList<>(settings.size());
		for (AppSetting setting : settings) {
			pkgList.add(setting.packageName);
		}
		return pkgList.toArray(new String[pkgList.size()]);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
