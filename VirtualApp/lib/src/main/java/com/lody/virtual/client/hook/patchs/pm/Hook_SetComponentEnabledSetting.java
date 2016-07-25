package com.lody.virtual.client.hook.patchs.pm;

import android.content.ComponentName;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#setComponentEnabledSetting(ComponentName, int, int, int)
 *
 *
 */
/* package */ class Hook_SetComponentEnabledSetting extends Hook {

	@Override
	public String getName() {
		return "setComponentEnabledSetting";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return 0;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
