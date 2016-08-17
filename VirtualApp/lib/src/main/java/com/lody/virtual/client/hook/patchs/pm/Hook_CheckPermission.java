package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

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
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		return VPackageManager.getInstance().checkPermission(permName, pkgName, userId);
	}

	@Override
	public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
		return super.afterHook(who, method, args, result);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
