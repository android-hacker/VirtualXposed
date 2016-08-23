package com.lody.virtual.client.hook.patchs.am;

import android.app.IActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.ServiceManager;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.hook.base.HookObject;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.StaticHook;

import java.lang.reflect.Method;

import mirror.android.app.ActivityManagerNative;
import mirror.android.util.Singleton;

/**
 * @author Lody
 * @see IActivityManager
 * @see android.app.ActivityManager
 */

@Patch({StartActivity.class, StartActivityAsCaller.class,
		StartActivityAndWait.class, StartActivityWithConfig.class, StartActivityIntentSender.class,
		StartNextMatchingActivity.class, StartVoiceActivity.class,
		GetIntentSender.class, RegisterReceiver.class, GetContentProvider.class,
		GetContentProviderExternal.class,

		GetActivityClassForToken.class, GetTasks.class, GetRunningAppProcesses.class,

		StartService.class, StopService.class, StopServiceToken.class, BindService.class,
		UnbindService.class, PeekService.class, ServiceDoneExecuting.class, UnbindFinished.class,
		PublishService.class,

		HandleIncomingUser.class, SetServiceForeground.class,

		BroadcastIntent.class, GetCallingPackage.class, GrantUriPermissionFromOwner.class,
		CheckGrantUriPermission.class, GetPersistedUriPermissions.class, KillApplicationProcess.class,
		ForceStopPackage.class, AddPackageDependency.class, UpdateDeviceOwner.class,
		CrashApplication.class, GetPackageForToken.class, GetPackageForIntentSender.class,

		SetPackageAskScreenCompat.class, GetPackageAskScreenCompat.class,
		CheckPermission.class, PublishContentProviders.class, GetCurrentUser.class,
		UnstableProviderDied.class, GetCallingActivity.class, FinishActivity.class,
		GetServices.class,

		SetTaskDescription.class,})

public class ActivityManagerPatch extends PatchObject<IActivityManager, HookObject<IActivityManager>> {


	@Override
	protected HookObject<IActivityManager> initHookObject() {
		return new HookObject<>(android.app.ActivityManagerNative.getDefault());
	}

	@Override
	public void inject() throws Throwable {
		if (ActivityManagerNative.gDefault.type() == IActivityManager.class) {
			ActivityManagerNative.gDefault.set(getHookObject().getProxyObject());

		} else if (ActivityManagerNative.gDefault.type() == android.util.Singleton.class) {
			Object gDefault = ActivityManagerNative.gDefault.get();
			Singleton.mInstance.set(gDefault, getHookObject().getProxyObject());
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
		return android.app.ActivityManagerNative.getDefault() != getHookObject().getProxyObject();
	}
}
