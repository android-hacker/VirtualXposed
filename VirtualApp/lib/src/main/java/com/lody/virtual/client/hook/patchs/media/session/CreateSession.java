package com.lody.virtual.client.hook.patchs.media.session;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class CreateSession extends Hook {


	@Override
	public String getName() {
		return "createSession";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		if (args[0] instanceof String) {
			args[0] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
