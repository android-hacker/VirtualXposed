package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 */

public class Hook_SetAppLockedVerifying extends Hook {

	@Override
	public String getName() {
		return "setAppLockedVerifying";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceAppPkg(args);
		return method.invoke(who, args);
	}
}
