package com.lody.virtual.client.hook.patchs.power;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_AcquireWakeLock extends Hook {

	@Override
	public String getName() {
		return "acquireWakeLock";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceSequenceAppPkg(args, 2);
		return method.invoke(who, args);
	}
}
