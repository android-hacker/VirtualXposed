package com.lody.virtual.client.hook.patchs.am;

import android.os.IBinder;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#getActivityClassForToken(IBinder)
 */
/* package */ class Hook_GetActivityClassForToken extends Hook {

	@Override
	public String getName() {
		return "getActivityClassForToken";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
