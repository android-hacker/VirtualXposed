package com.lody.virtual.client.hook.patchs.appwidget;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.content.ComponentName;

/**
 * @author Lody
 *
 *
 * @see com.android.internal.appwidget.IAppWidgetService#getAppWidgetIds(ComponentName)
 */
/* package */ class Hook_GetAppWidgetIds extends Hook<AppWidgetManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetAppWidgetIds(AppWidgetManagerPatch patchObject) {
		super(patchObject);
	}

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
