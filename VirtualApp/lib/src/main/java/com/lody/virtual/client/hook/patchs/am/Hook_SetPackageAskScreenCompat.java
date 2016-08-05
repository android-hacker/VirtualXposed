package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.os.Build;

/**
 * @author Lody
 *
 */
/* package */ class Hook_SetPackageAskScreenCompat extends Hook {

	@Override
	public String getName() {
		return "setPackageAskScreenCompat";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			if (args.length > 0 && args[0] instanceof String) {
				String pkg = (String) args[0];
				if (isAppPkg(pkg)) {
					args[0] = getHostPkg();
				}
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
