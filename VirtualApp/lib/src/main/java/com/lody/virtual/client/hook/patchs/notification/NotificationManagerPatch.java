package com.lody.virtual.client.hook.patchs.notification;

import android.os.Build;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookDelegate;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.StaticHook;

import mirror.android.app.NotificationManager;
import mirror.android.widget.Toast;

/**
 * @author Lody
 * @see android.app.NotificationManager
 * @see android.widget.Toast
 */
@Patch({CancelAllNotifications.class, EnqueueNotificationWithTag.class, CancelNotificationWithTag.class,
        EnqueueNotificationWithTagPriority.class, EnqueueNotification.class,
        SetNotificationsEnabledForPackage.class,
        AreNotificationsEnabledForPackage.class})
public class NotificationManagerPatch extends PatchDelegate<HookDelegate<IInterface>> {

    public NotificationManagerPatch() {
        super(new HookDelegate<IInterface>(NotificationManager.getService.call()));
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new ReplaceCallingPkgHook("enqueueToast"));
        addHook(new ReplaceCallingPkgHook("cancelToast"));
        addHook(new StaticHook("registerListener"));
        addHook(new StaticHook("unregisterListener"));
        addHook(new StaticHook("getAppActiveNotifications"));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addHook(new ReplaceCallingPkgHook("areNotificationsEnabled"));
        }
        if ("samsung".equalsIgnoreCase(Build.BRAND) || "samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
            addHook(new ReplaceCallingPkgHook("removeEdgeNotification"));
        }
    }

    @Override
    public void inject() throws Throwable {
        NotificationManager.sService.set(getHookDelegate().getProxyInterface());
        Toast.sService.set(getHookDelegate().getProxyInterface());
    }

    @Override
    public boolean isEnvBad() {
        return NotificationManager.getService.call() != getHookDelegate().getProxyInterface();
    }
}
