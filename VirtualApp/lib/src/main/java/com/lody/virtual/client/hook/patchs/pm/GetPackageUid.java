package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class GetPackageUid extends Hook {

	@Override
	public String getName() {
		return "getPackageUid";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (pkgName.equals(getHostPkg())) {
			return method.invoke(who, args);
		}
		int uid = VPackageManager.get().getPackageUid(pkgName, 0);
		return VUserHandle.getAppId(uid);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}

}
