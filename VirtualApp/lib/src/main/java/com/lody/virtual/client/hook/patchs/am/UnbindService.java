package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;

import android.app.IServiceConnection;

/**
 * @author Lody
 *
 *         原型: public boolean unbindService(IServiceConnection connection)
 */
/* package */ class UnbindService extends Hook {

	@Override
	public String getName() {
		return "unbindService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IServiceConnection connection = (IServiceConnection) args[0];
		return VActivityManager.get().unbindService(connection);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
