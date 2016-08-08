package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class Hook_StartActivityAsCaller extends Hook_StartActivity {

	@Override
	public String getName() {
		return "startActivityAsCaller";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return super.onHook(who, method, args);
	}
}
