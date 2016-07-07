package com.lody.virtual.client.hook.patchs.notification;

import java.lang.reflect.Field;

import com.lody.virtual.client.hook.base.HookObject;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;

import android.app.INotificationManager;
import android.app.NotificationManager;
import android.widget.Toast;

/**
 * @author Lody
 *
 *
 * @see INotificationManager
 * @see NotificationManager
 * @see Toast
 */
@Patch({Hook_EnqueueToast.class, Hook_CancelToast.class, Hook_CancelAllNotifications.class,
		Hook_EnqueueNotificationWithTag.class, Hook_CancelNotificationWithTag.class,})
public class NotificationManagerPatch extends PatchObject<INotificationManager, HookObject<INotificationManager>> {

	public static INotificationManager getNM() {
		return NotificationManager.getService();
	}

	@Override
	protected HookObject<INotificationManager> initHookObject() {
		return new HookObject<INotificationManager>(getNM());
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
