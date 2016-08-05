package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalServiceManager;

import android.content.ComponentName;
import android.os.IBinder;

/**
 * @author Lody
 *
 *         原型: public boolean stopServiceToken(ComponentName className, IBinder
 *         token, int startId)
 */
/* package */ class Hook_StopServiceToken extends Hook {

	@Override
	public String getName() {
		return "stopServiceToken";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName componentName = (ComponentName) args[0];
		IBinder token = (IBinder) args[1];
		int startId = (int) args[2];
		if (componentName != null) {
			String pkgName = componentName.getPackageName();
			if (isAppPkg(pkgName)) {
				return LocalServiceManager.getInstance().stopServiceToken(componentName, token, startId);
			}
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
