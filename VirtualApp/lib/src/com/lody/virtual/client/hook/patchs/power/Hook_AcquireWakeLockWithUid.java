package com.lody.virtual.client.hook.patchs.power;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import android.os.IBinder;

/**
 * @author Lody
 *
 *
 * @see android.os.IPowerManager#acquireWakeLockWithUid(IBinder, int, String,
 *      String, int)
 */
/* package */ class Hook_AcquireWakeLockWithUid extends Hook<PowerManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_AcquireWakeLockWithUid(PowerManagerPatch patchObject) {
		super(patchObject);
	}

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
