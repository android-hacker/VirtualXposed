package com.lody.virtual.client.hook.patchs.user;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_GetUserForSerialNumber extends Hook {

	@Override
	public String getName() {
		return "getUserForSerialNumber";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		return method.invoke(who, args);
	}
}
