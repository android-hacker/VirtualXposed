package com.lody.virtual.client.hook.patchs.am;

import android.app.IServiceConnection;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.secondary.ServiceConnectionDelegate;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * public boolean unbindService(IServiceConnection connection)
 */
/* package */ class UnbindService extends Hook {

	@Override
	public String getName() {
		return "unbindService";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		IServiceConnection conn = (IServiceConnection) args[0];
		ServiceConnectionDelegate delegate = ServiceConnectionDelegate.removeDelegate(conn);
		if (delegate == null) {
			return method.invoke(who, args);
		}
		return VActivityManager.get().unbindService(delegate);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServerProcess();
	}
}
