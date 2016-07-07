package com.lody.virtual.client.hook.patchs.appwidget;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_BindRemoteViewsService extends Hook<AppWidgetManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_BindRemoteViewsService(AppWidgetManagerPatch patchObject) {
		super(patchObject);
	}

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
