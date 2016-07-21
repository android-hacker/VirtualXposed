package com.lody.virtual.client.hook.patchs.am;

import android.os.IBinder;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

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
		String pkg = AppSandBox.getLastPkg();
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
