package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

/**
 * Class: Created by andy on 16-8-3. TODO:
 */
public class Hook_StartNextMatchingActivity extends Hook_BaseStartActivity {
	@Override
	public String getName() {
		return "startNextMatchingActivity";
	}

	@Override
	public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
		return super.afterHook(who, method, args, result);
	}
}
