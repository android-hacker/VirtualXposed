package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.ApplicationInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#getApplicationInfo(String, int, int)
 */
/* package */ class Hook_GetApplicationInfo extends Hook {

	@Override
	public String getName() {
		return "getApplicationInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkg = (String) args[0];
		int flags = (int) args[1];
		if (getHostPkg().equals(pkg)) {
			if (args.length > 2 && args[2] instanceof Integer) {
				args[2] = VirtualCore.getCore().myUserId();
			}
			return method.invoke(who, args);
		}
		int userId = VUserHandle.myUserId();
		if (args.length > 2 && args[2] instanceof Integer) {
			userId = (int) args[2];
		}
		ApplicationInfo applicationInfo = VPackageManager.getInstance().getApplicationInfo(pkg, flags, userId);
		if (applicationInfo != null) {
			return applicationInfo;
		}
		if (args.length > 2 && args[2] instanceof Integer) {
			args[2] = VirtualCore.getCore().myUserId();
		}
		applicationInfo = (ApplicationInfo) method.invoke(who, args);
		if (applicationInfo != null) {
			if (ComponentUtils.isSystemApp(applicationInfo)) {
				return applicationInfo;
			}
		}
		return null;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
