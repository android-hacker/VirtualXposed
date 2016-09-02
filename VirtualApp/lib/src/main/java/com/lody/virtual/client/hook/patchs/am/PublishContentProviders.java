package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
@SuppressWarnings("unchecked")
/* package */ class PublishContentProviders extends Hook {

	@Override
	public String getName() {
		return "publishContentProviders";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
