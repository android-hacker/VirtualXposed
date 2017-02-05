package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.PackageInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.helper.utils.ComponentUtils;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
/**
 * @author Lody
 *
 *
 *         public PackageInfo getPackageInfo(String packageName, int flags,
 *         int userId)
 */
public final class GetPackageInfo extends Hook {

	@Override
	public String getName() {
		return "getPackageInfo";
	}

	@Override
	public boolean beforeCall(Object who, Method method, Object... args) {
		return args != null && args[0] != null;
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String pkg = (String) args[0];
		int flags = (int) args[1];
		int userId = VUserHandle.myUserId();
		long before = System.currentTimeMillis();
		PackageInfo packageInfo = VPackageManager.get().getPackageInfo(pkg, flags, userId);
		long delta = System.currentTimeMillis() - before;
		if (packageInfo != null) {
			VLog.d(getClass().getSimpleName(), "get pkg : " + pkg + " spend " + delta + "ms.");
			return packageInfo;
		}
		packageInfo = (PackageInfo) method.invoke(who, args);
		if (packageInfo != null) {
			if (getHostPkg().equals(packageInfo.packageName) || ComponentUtils.isSystemApp(packageInfo)) {
				return packageInfo;
			}
		}
		return null;
	}

}
