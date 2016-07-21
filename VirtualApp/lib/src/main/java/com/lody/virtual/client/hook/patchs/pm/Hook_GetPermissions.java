package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 *
 */
/* package */ class Hook_GetPermissions extends Hook {

	@Override
	public String getName() {
		return "getPermissions";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return method.invoke(who, args);
	}
}
