package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 */
public class StartActivityIntentSender extends Hook {
	@Override
	public String getName() {
		return "startActivityIntentSender";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {

		return super.call(who, method, args);
	}
}
