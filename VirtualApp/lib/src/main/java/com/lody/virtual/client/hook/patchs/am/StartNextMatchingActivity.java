package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

public class StartNextMatchingActivity extends BaseStartActivity {
	@Override
	public String getName() {
		return "startNextMatchingActivity";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return false;
	}
}
