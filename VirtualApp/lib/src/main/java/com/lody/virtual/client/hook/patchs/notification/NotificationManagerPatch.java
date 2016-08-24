package com.lody.virtual.client.hook.patchs.notification;

import android.app.INotificationManager;
import android.app.NotificationManager;
import android.os.Build;
import android.widget.Toast;

import com.lody.virtual.client.hook.base.HookObject;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.StaticHook;

import java.lang.reflect.Field;

/**
 * @author Lody
 *
 *
 * @see INotificationManager
 * @see NotificationManager
 * @see Toast
 */
@Patch({CancelAllNotifications.class, EnqueueNotificationWithTag.class, CancelNotificationWithTag.class,
		EnqueueNotificationWithTagPriority.class, EnqueueNotification.class})
public class NotificationManagerPatch extends PatchObject<INotificationManager, HookObject<INotificationManager>> {

	public static INotificationManager getNM() {
		return NotificationManager.getService();
	}

	@Override
	protected HookObject<INotificationManager> initHookObject() {
		return new HookObject<INotificationManager>(getNM());
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ReplaceCallingPkgHook("enqueueToast"));
		addHook(new ReplaceCallingPkgHook("cancelToast"));
		addHook(new ReplaceCallingPkgHook("areNotificationsEnabledForPackage"));
		addHook(new StaticHook("registerListener"));
		addHook(new StaticHook("unregisterListener"));
		addHook(new StaticHook("getAppActiveNotifications"));
		if ("samsung".equalsIgnoreCase(Build.BRAND)) {
			addHook(new ReplaceCallingPkgHook("removeEdgeNotification"));
		}
	}

	@Override
	public void inject() throws Throwable {
		HookObject<INotificationManager> hookedNM = getHookObject();
		Field f_sService = NotificationManager.class.getDeclaredField("sService");
		f_sService.setAccessible(true);
		f_sService.set(null, hookedNM.getProxyObject());
		try {
			f_sService = Toast.class.getDeclaredField("sService");
			f_sService.setAccessible(true);
			f_sService.set(null, hookedNM.getProxyObject());
		} catch (Throwable e) {
			// Ignore
		}
	}

	@Override
	public boolean isEnvBad() {
		return getNM() != getHookObject().getProxyObject();
	}
}
