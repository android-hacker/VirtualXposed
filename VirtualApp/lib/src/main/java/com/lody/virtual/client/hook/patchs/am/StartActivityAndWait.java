package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

/*package*/ class StartActivityAndWait extends BaseStartActivity {
	@Override
	public String getName() {
		return "startActivityAndWait";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return super.call(who, method, args);
	}
}
