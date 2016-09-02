package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.content.ComponentName;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#setComponentEnabledSetting(ComponentName,
 *      int, int, int)
 *
 *
 */
/* package */ class SetComponentEnabledSetting extends Hook {

	@Override
	public String getName() {
		return "setComponentEnabledSetting";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return 0;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
