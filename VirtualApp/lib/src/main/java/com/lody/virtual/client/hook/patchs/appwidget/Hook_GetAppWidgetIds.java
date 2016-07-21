package com.lody.virtual.client.hook.patchs.appwidget;

import android.content.ComponentName;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see com.android.internal.appwidget.IAppWidgetService#getAppWidgetIds(ComponentName)
 */
/* package */ class Hook_GetAppWidgetIds extends Hook {

	@Override
	public String getName() {
		return "getAppWidgetIds";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName componentName = (ComponentName) args[0];
		if (componentName != null && isAppPkg(componentName.getPackageName())) {
			return new int[]{
					// Empty
			};
		}
		return method.invoke(who, args);
	}
}
