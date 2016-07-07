package com.lody.virtual.client.hook.patchs.notification;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;

/**
 * @author Lody
 *
 */
/* package */ class Hook_EnqueueToast extends Hook<NotificationManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_EnqueueToast(NotificationManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "enqueueToast";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		if (!VirtualCore.getCore().isHostPackageName(pkgName)) {
			args[0] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
