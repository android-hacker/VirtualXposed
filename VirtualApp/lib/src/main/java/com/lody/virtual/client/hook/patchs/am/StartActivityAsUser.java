package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class StartActivityAsUser extends StartActivity {

	@Override
	public String getName() {
		return "startActivityAsUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return super.onHook(who, method, args);
	}
}
