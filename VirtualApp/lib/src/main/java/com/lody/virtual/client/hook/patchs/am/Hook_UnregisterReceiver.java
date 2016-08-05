package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * Class: Created by andy on 16-8-3. TODO:
 */
public class Hook_UnregisterReceiver extends Hook {
	@Override
	public String getName() {
		return "unregisterReceiver";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		return super.onHook(who, method, args);
	}
}
