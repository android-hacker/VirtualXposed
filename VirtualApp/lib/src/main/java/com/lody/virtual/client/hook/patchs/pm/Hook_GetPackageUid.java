package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.utils.VLog;

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
		int vuid = VPackageManager.getInstance().getPackageUid(pkgName);
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
