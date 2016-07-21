package com.lody.virtual.client.hook.patchs.telephony;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see com.android.internal.telephony.ITelephony#getDeviceId(String)
 *
 */
/* package */ class Hook_GetDeviceId extends Hook {

	@Override
	public String getName() {
		return "getDeviceId";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}

}
