package com.lody.virtual.client.hook.patchs.appwidget;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_IsBoundWidgetPackage extends Hook {

	@Override
	public String getName() {
		return "isBoundWidgetPackage";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (isAppPkg(pkgName)) {
			return false;
		}
		return method.invoke(who, args);
	}
}
