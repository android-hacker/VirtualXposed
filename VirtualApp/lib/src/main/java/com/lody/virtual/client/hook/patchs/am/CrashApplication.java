package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#crashApplication(int, int, String, String)
 */
/* package */ class CrashApplication extends Hook {

	@Override
	public String getName() {
		return "crashApplication";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
