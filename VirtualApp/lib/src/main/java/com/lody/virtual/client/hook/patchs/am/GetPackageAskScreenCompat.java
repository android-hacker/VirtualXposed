package com.lody.virtual.client.hook.patchs.am;

import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class GetPackageAskScreenCompat extends Hook {

	@Override
	public String getName() {
		return "getPackageAskScreenCompat";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
			if (args.length > 0 && args[0] instanceof String) {
				args[0] = getHostPkg();
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
