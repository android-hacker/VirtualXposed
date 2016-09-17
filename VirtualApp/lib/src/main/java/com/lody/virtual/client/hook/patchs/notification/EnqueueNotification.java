package com.lody.virtual.client.hook.patchs.notification;

import android.app.Notification;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.patchs.notification.compat.NotificationHandler;
import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class EnqueueNotification extends Hook {

	@Override
	public String getName() {
		return "enqueueNotification";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String pkg = (String) args[0];
		int notificationIndex = ArrayUtils.indexOfFirst(args, Notification.class);
		Notification notification = (Notification) args[notificationIndex];
		NotificationHandler.Result result = NotificationHandler.getInstance()
				.dealNotification(getHostContext(), notification, pkg);
		if (result.code == NotificationHandler.RES_NOT_SHOW) {
			return 0;
		} else if (result.code == NotificationHandler.RES_REPLACE) {
			args[notificationIndex] = result.notification;
		}
		args[0] = getHostPkg();
		if (getName().endsWith("WithTag") && Build.VERSION.SDK_INT >= 18 && args[1] instanceof String) {
			args[1] = getHostPkg();
		}
		return method.invoke(who, args);
	}
}
