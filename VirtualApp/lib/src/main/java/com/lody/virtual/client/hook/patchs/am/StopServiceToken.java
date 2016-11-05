package com.lody.virtual.client.hook.patchs.am;

import android.content.ComponentName;
import android.os.IBinder;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *         原型: public boolean stopServiceToken(ComponentName className, IBinder
 *         token, int startId)
 */
/* package */ class StopServiceToken extends Hook {

	@Override
	public String getName() {
		return "stopServiceToken";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		ComponentName componentName = (ComponentName) args[0];
		IBinder token = (IBinder) args[1];
		if (!VActivityManager.get().isVAServiceToken(token)) {
			return method.invoke(who, args);
		}
		int startId = (int) args[2];
		if (componentName != null) {
			return VActivityManager.get().stopServiceToken(componentName, token, startId);
		}
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServerProcess();
	}
}
