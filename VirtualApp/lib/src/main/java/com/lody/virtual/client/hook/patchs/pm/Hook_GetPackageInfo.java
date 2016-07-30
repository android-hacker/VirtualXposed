package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.PackageInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import java.lang.reflect.Method;
/**
 * @author Lody
 *
 *
 *         适配插件的包信息获取.
 *
 *         原型: public PackageInfo getPackageInfo(String packageName, int flags,
 *         int userId)
 */
public final class Hook_GetPackageInfo extends Hook {


	@Override
	public String getName() {
		return "getPackageInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkg = (String) args[0];
		int flags = (int) args[1];
		PackageInfo packageInfo = LocalPackageManager.getInstance().getPackageInfo(pkg, flags);
		if (packageInfo != null) {
			return packageInfo;
		}
		return method.invoke(who, args);
	}

}
