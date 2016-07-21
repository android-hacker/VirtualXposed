package com.lody.virtual.client.hook.patchs.appwidget;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_BindRemoteViewsService extends Hook {

	@Override
	public String getName() {
		return "bindRemoteViewsService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String callingPkgName = (String) args[0];
		if (isAppPkg(callingPkgName)) {
			return 0;
		}
		return method.invoke(who, args);
	}
}
