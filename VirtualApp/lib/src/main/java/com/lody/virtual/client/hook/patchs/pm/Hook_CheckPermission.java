package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager#checkPermission(String, String, int)
 */
/* package */ class Hook_CheckPermission extends Hook {

	@Override
	public String getName() {
		return "checkPermission";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String permName = (String) args[0];
		String pkgName = (String) args[1];
		return LocalPackageManager.getInstance().checkPermission(permName, pkgName);
	}

	@Override
	public boolean isEnable() {
		return false;
	}
}
