package com.lody.virtual.client.hook.patchs.power;

import android.os.IBinder;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.os.IPowerManager#acquireWakeLockWithUid(IBinder, int, String,
 *      String, int)
 */
/* package */ class Hook_AcquireWakeLockWithUid extends Hook {

	@Override
	public String getName() {
		return "acquireWakeLockWithUid";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		return method.invoke(who, args);
	}
}
