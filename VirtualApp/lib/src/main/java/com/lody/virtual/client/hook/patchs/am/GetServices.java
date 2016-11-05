package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#getServices(int, int)
 *
 */
public class GetServices extends Hook {
	@Override
	public String getName() {
		return "getServices";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		int maxNum = (int) args[0];
		int flags = (int) args[1];
		return VActivityManager.get().getServices(maxNum, flags).getList();
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
