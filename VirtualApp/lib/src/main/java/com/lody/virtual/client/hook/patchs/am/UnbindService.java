package com.lody.virtual.client.hook.patchs.am;

import android.app.IServiceConnection;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.secondary.HackServiceConnection;
import com.lody.virtual.client.local.VActivityManager;

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
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IServiceConnection conn = (IServiceConnection) args[0];
		if (conn != null) {
			HackServiceConnection hackConn = HackServiceConnection.sHackConns.remove(conn.asBinder());
			return VActivityManager.get().unbindService(hackConn != null ? hackConn : conn);
		}
		return false;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess() || isServiceProcess();
	}
}
