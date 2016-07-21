package com.lody.virtual.client.hook.patchs.restriction;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

/* package */ class Hook_NotifyPermissionResponse extends Hook {

	@Override
	public String getName() {
		return "notifyPermissionResponse";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
