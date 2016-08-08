package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;

import android.os.IBinder;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#getCallingPackage(IBinder)
 */
/* package */ class Hook_GetCallingPackage extends Hook {

	@Override
	public String getName() {
		return "getCallingPackage";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IBinder token = (IBinder) args[0];
		String pkg = VActivityManager.getInstance().getPackageForToken(token);
		if (pkg != null) {
			return pkg;
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
