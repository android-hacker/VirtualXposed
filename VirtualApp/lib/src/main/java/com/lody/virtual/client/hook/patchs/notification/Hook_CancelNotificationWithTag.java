package com.lody.virtual.client.hook.patchs.notification;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_CancelNotificationWithTag extends Hook<NotificationManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_CancelNotificationWithTag(NotificationManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "cancelNotificationWithTag";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (!VirtualCore.getCore().isHostPackageName(pkgName)) {
            args[0] = VirtualCore.getCore().getContext().getPackageName();
		}
		return method.invoke(who, args);
	}
}
