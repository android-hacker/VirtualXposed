package com.lody.virtual.client.hook.patchs.am;

import android.app.IServiceConnection;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalServiceManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *         原型: public boolean unbindService(IServiceConnection connection)
 */
/* package */ class Hook_UnbindService extends Hook {

	@Override
	public String getName() {
		return "unbindService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IServiceConnection connection = (IServiceConnection) args[0];
		return LocalServiceManager.getInstance().unbindService(connection);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
