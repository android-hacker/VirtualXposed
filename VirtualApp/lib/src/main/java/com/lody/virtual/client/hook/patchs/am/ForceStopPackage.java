package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class ForceStopPackage extends Hook {

	@Override
	public String getName() {
		return "forceStopPackage";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String pkg = (String) args[0];
		int userId = VUserHandle.myUserId();
		VActivityManager.get().killAppByPkg(pkg, userId);
		return 0;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
