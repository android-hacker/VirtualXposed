package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;

import android.os.IBinder;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#serviceDoneExecuting(IBinder, int, int,
 *      int)
 *
 */
/* package */ class Hook_ServiceDoneExecuting extends Hook {

	@Override
	public String getName() {
		return "serviceDoneExecuting";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IBinder token = (IBinder) args[0];
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
