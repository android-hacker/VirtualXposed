package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#forceStopPackage(String, int)
 */
/* package */ class Hook_ForceStopPackage extends Hook {

	@Override
	public String getName() {
		return "forceStopPackage";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args.length > 0 && args[0] instanceof String) {
			String pkg = (String) args[0];
			if (isAppPkg(pkg)) {
				VActivityManager.getInstance().killAppByPkg(pkg);
				return 0;
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
