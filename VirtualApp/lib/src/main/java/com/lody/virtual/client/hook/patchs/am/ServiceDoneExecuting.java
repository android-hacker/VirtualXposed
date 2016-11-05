package com.lody.virtual.client.hook.patchs.am;

import android.os.IBinder;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 */
/* package */ class ServiceDoneExecuting extends Hook {

	@Override
	public String getName() {
		return "serviceDoneExecuting";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		IBinder token = (IBinder) args[0];
		if (!VActivityManager.get().isVAServiceToken(token)) {
			return method.invoke(who, args);
		}
		int type = (int) args[1];
		int startId = (int) args[2];
		int res = (int) args[3];
		VActivityManager.get().serviceDoneExecuting(token, type, startId, res);
		return 0;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
