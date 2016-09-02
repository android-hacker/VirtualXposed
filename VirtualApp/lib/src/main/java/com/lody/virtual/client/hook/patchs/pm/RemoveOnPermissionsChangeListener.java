package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 */

public class RemoveOnPermissionsChangeListener extends Hook {

	@Override
	public String getName() {
		return "removeOnPermissionsChangeListener";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return 0;
	}
}
