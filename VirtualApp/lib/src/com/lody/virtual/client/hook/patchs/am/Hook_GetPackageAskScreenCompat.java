package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.os.Build;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetPackageAskScreenCompat extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetPackageAskScreenCompat(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getPackageAskScreenCompat";
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
