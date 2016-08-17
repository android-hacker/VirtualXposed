package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.utils.VLog;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#getPackageUid(String, int)
 */
/* package */ class Hook_GetPackageUid extends Hook {

	@Override
	public String getName() {
		return "getPackageUid";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 1 && args[1] instanceof Integer) {
			userId = (int) args[1];
		}
		int vuid = VPackageManager.getInstance().getPackageUid(pkgName, userId);
		if (vuid == -1 && pkgName.equals(getHostPkg())) {
			return method.invoke(who, args);
		}
		VLog.d(getName(), "getPackageUid return %d.", vuid);
		return vuid;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
