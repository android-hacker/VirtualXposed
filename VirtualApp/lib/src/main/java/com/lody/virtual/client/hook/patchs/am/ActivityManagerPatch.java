package com.lody.virtual.client.hook.patchs.am;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.ServiceManager;
import android.util.Singleton;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.hook.base.HookObject;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.StaticHook;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author Lody
 * @see ActivityManagerNative
 * @see IActivityManager
 * @see android.app.ActivityManager
 */

@Patch({Hook_StartActivities.class, Hook_StartActivity.class, Hook_StartActivityAsCaller.class,
		Hook_StartActivityAndWait.class, Hook_StartActivityWithConfig.class, Hook_StartActivityIntentSender.class,
		Hook_StartNextMatchingActivity.class, Hook_StartVoiceActivity.class,
		Hook_GetIntentSender.class, Hook_RegisterReceiver.class, Hook_GetContentProvider.class,
		Hook_GetContentProviderExternal.class,

		Hook_GetActivityClassForToken.class, Hook_GetTasks.class, Hook_GetRunningAppProcesses.class,

		Hook_StartService.class, Hook_StopService.class, Hook_StopServiceToken.class, Hook_BindService.class,
		Hook_UnbindService.class, Hook_PeekService.class, Hook_ServiceDoneExecuting.class, Hook_UnbindFinished.class,
		Hook_PublishService.class,

		Hook_HandleIncomingUser.class, Hook_SetServiceForeground.class,

		Hook_BroadcastIntent.class, Hook_GetCallingPackage.class, Hook_GrantUriPermissionFromOwner.class,
		Hook_CheckGrantUriPermission.class, Hook_GetPersistedUriPermissions.class, Hook_KillApplicationProcess.class,
		Hook_ForceStopPackage.class, Hook_AddPackageDependency.class, Hook_UpdateDeviceOwner.class,
		Hook_CrashApplication.class, Hook_GetPackageForToken.class,

		Hook_SetPackageAskScreenCompat.class, Hook_GetPackageAskScreenCompat.class,
		Hook_CheckPermission.class, Hook_PublishContentProviders.class, Hook_GetCurrentUser.class,
		Hook_UnstableProviderDied.class, Hook_GetCallingActivity.class, Hook_FinishActivity.class,
		Hook_GetServices.class,})

public class ActivityManagerPatch extends PatchObject<IActivityManager, HookObject<IActivityManager>> {

	public static IActivityManager getAMN() {
		return ActivityManagerNative.getDefault();
	}

	@Override
	protected HookObject<IActivityManager> initHookObject() {
		return new HookObject<IActivityManager>(getAMN());
	}

	@Override
	public void inject() throws Throwable {

		Field f_gDefault = ActivityManagerNative.class.getDeclaredField("gDefault");
		if (!f_gDefault.isAccessible()) {
			f_gDefault.setAccessible(true);
		}
		if (f_gDefault.getType() == IActivityManager.class) {
			f_gDefault.set(null, getHookObject().getProxyObject());

		} else if (f_gDefault.getType() == Singleton.class) {

			Singleton gDefault = (Singleton) f_gDefault.get(null);
			Field f_mInstance = Singleton.class.getDeclaredField("mInstance");
			if (!f_mInstance.isAccessible()) {
				f_mInstance.setAccessible(true);
			}
			f_mInstance.set(gDefault, getHookObject().getProxyObject());
		} else {
			// 不会经过这里
			throw new UnsupportedOperationException("Singleton is not visible in AMN.");
		}

		HookBinder<IActivityManager> hookAMBinder = new HookBinder<IActivityManager>() {
			@Override
			protected IBinder queryBaseBinder() {
				return ServiceManager.getService(Context.ACTIVITY_SERVICE);
			}

			@Override
			protected IActivityManager createInterface(IBinder baseBinder) {
				return getHookObject().getProxyObject();
			}
		};
		hookAMBinder.injectService(Context.ACTIVITY_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		if (VirtualCore.getCore().isVAppProcess()) {
			addHook(new isUserRunning());
			addHook(new ReplaceCallingPkgHook("setAppLockedVerifying"));
			addHook(new StaticHook("checkUriPermission") {
				@Override
				public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
					return PackageManager.PERMISSION_GRANTED;
				}
			});
			addHook(new StaticHook("checkPermissionWithToken")).replaceUid(2);
			addHook(new StaticHook("broadcastStickyIntent")).replaceLastUserId();
			addHook(new StaticHook("startActivityAsUser")).replaceLastUserId();
			addHook(new StaticHook("unbroadcastIntent")).replaceLastUserId();
			addHook(new StaticHook("getRecentTasks")).replaceLastUserId();
			addHook(new StaticHook("grantUriPermission")).replaceLastUserId();
			addHook(new StaticHook("revokeUriPermission")).replaceLastUserId();
			addHook(new StaticHook("takePersistableUriPermission")).replaceLastUserId();
			addHook(new StaticHook("releasePersistableUriPermission")).replaceLastUserId();
			addHook(new StaticHook("profileControl")).replaceUserId(1);
			addHook(new StaticHook("getProviderMimeType")).replaceLastUserId();
			addHook(new StaticHook("dumpHeap")).replaceUserId(0);
			addHook(new StaticHook("killUid")).replaceUserId(1);
			addHook(new StaticHook("updateLockTaskPackages")).replaceUserId(0);
			addHook(new StaticHook("setProcessMemoryTrimLevel")).replaceUserId(1);
		}
	}

	private class isUserRunning extends Hook {
		@Override
		public String getName() {
			return "isUserRunning";
		}

		@Override
		public boolean beforeHook(Object who, Method method, Object... args) {
			int userId = (int) args[0];
			return userId == 0;
		}
	}

	@Override
	public boolean isEnvBad() {
		return getAMN() != getHookObject().getProxyObject();
	}
}
