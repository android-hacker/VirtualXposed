package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

class StartNextMatchingActivity extends StartActivity {
	@Override
	public String getName() {
		return "startNextMatchingActivity";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return false;
	}
}
