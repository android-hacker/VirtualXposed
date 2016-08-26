package com.lody.virtual.client.hook.patchs.notification;

import android.app.NotificationManager;
import android.os.Build;
import android.os.IInterface;
import android.widget.Toast;

import com.lody.virtual.client.hook.base.HookDelegate;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.StaticHook;

/**
 * @author Lody
 *
 *
 * @see NotificationManager
 * @see Toast
 */
@Patch({CancelAllNotifications.class, EnqueueNotificationWithTag.class, CancelNotificationWithTag.class,
		EnqueueNotificationWithTagPriority.class, EnqueueNotification.class})
public class NotificationManagerPatch extends PatchDelegate<HookDelegate<IInterface>> {

	@Override
	protected HookDelegate<IInterface> createHookDelegate() {
		return new HookDelegate<IInterface>() {
			@Override
			protected IInterface createInterface() {
				return mirror.android.app.NotificationManager.getService.call();
			}
		};
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
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
		mirror.android.app.NotificationManager.sService.set(getHookDelegate().getProxyInterface());
		mirror.android.widget.Toast.sService.set(getHookDelegate().getProxyInterface());
	}

	@Override
	public boolean isEnvBad() {
		return mirror.android.app.NotificationManager.getService.call() != getHookDelegate().getProxyInterface();
	}
}
