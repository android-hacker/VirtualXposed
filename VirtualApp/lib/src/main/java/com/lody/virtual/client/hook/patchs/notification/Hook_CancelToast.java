package com.lody.virtual.client.hook.patchs.notification;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_CancelToast extends Hook {


	@Override
	public String getName() {
		return "cancelToast";
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
