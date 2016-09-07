package com.lody.virtual.client.hook.patchs.pm;

import android.content.ComponentName;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class GetComponentEnabledSetting extends Hook {

	@Override
	public String getName() {
		return "getComponentEnabledSetting";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		// NOTE: 有4个状态: 0默认 1可用 2禁止 3User Disable
		ComponentName component = (ComponentName) args[0];
		if (component != null) {
			return 1;
		}
		return method.invoke(who, args);
	}
}
