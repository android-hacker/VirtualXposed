package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.local.LocalProcessManager;
import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#forceStopPackage(String, int)
 */
/* package */ class Hook_ForceStopPackage extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_ForceStopPackage(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "forceStopPackage";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args.length > 0 && args[0] instanceof String) {
			String pkg = (String) args[0];
			if (isAppPkg(pkg)) {
				LocalProcessManager.killAppByPkg(pkg);
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
