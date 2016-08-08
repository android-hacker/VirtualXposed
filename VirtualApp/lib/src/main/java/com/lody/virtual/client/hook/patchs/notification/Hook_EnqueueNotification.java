package com.lody.virtual.client.hook.patchs.notification;

import java.lang.reflect.Method;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.patchs.notification.compat.NotificationHandler;

import android.app.Notification;

/**
 * @author Lody
 */
/* package */ class Hook_EnqueueNotification extends Hook {

	@Override
	public String getName() {
		return "enqueueNotification";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String pkgName = (String) args[0];
		boolean needDeal = false;
		if (!VirtualCore.getCore().isHostPackageName(pkgName)) {
			args[0] = getHostPkg();
			needDeal = true;
		}
		if (needDeal) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] instanceof Notification) {
					Notification notification = (Notification) args[i];
					NotificationHandler.Result result = NotificationHandler.getInstance()
							.dealNotification(getHostContext(), notification, pkgName);
					if (result.code == NotificationHandler.RESULT_CODE_DONT_SHOW) {
						return 0;
					} else if (result.code == NotificationHandler.RESULT_CODE_REPLACE) {
						args[i] = result.notification;
					}
					break;
				}
			}
		}
		return method.invoke(who, args);
	}
}
