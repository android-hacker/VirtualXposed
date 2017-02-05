package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

/*package*/ class StartActivityWithConfig extends StartActivity {
	@Override
	public String getName() {
		return "startActivityWithConfig";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return super.call(who, method, args);
	}
}
