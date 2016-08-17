package com.lody.virtual.client.hook.patchs.notification;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_CancelNotificationWithTag extends Hook {
	{
		replaceLastUserId();
	}

	@Override
	public String getName() {
		return "cancelNotificationWithTag";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
