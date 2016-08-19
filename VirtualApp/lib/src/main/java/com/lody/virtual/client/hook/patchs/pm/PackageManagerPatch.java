package com.lody.virtual.client.hook.patchs.pm;

import android.app.ActivityThread;
import android.content.pm.IPackageManager;
import android.os.IBinder;

import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.hook.base.HookObject;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.ResultStaticHook;
import com.lody.virtual.client.interfaces.IHookObject;

import java.lang.reflect.Field;

/**
 * @author Lody
 *
 *
 * @see IPackageManager
 * @see android.app.ApplicationPackageManager
 */
@Patch({GetPackageInfo.class, GetApplicationInfo.class, GetActivityInfo.class, GetServiceInfo.class,
		GetPermissions.class, GetProviderInfo.class, GetReceiverInfo.class,
		GetPermissionFlags.class,

		CheckSignatures.class,

		ResolveIntent.class, ResolveService.class, ResolveContentProvider.class,

		QueryIntentActivities.class, QueryIntentServices.class, QueryIntentReceivers.class,
		QueryIntentContentProviders.class, QueryContentProviders.class,

		CheckPermission.class, RevokeRuntimePermission.class,

		SetPackageStoppedState.class, GetInstalledPackages.class, GetInstalledApplications.class,
		SetApplicationEnabledSetting.class, GetApplicationEnabledSetting.class,
		SetApplicationBlockedSettingAsUser.class, GetApplicationBlockedSettingAsUser.class,
		DeleteApplicationCacheFiles.class, GetInstallerPackageName.class,

		IsPackageAvailable.class, IsPackageForzen.class, GetComponentEnabledSetting.class,
		SetComponentEnabledSetting.class,

		GetPackageUid.class, GetPackageUidEtc.class,
		GetPackageGids.class,  GetPackageGidsEtc.class,
		GetPackagesForUid.class,

		AddPackageToPreferred.class, RemovePackageFromPreferred.class,
		ClearPackagePreferredActivities.class, GetPreferredActivities.class,
		ClearPackagePersistentPreferredActivities.class, GetPermissionGroupInfo.class,
		DeletePackage.class, GetPackageInstaller.class, AddOnPermissionsChangeListener.class,
		RemoveOnPermissionsChangeListener.class, ActivitySupportsIntent.class,

})
public final class PackageManagerPatch extends PatchObject<IPackageManager, HookObject<IPackageManager>> {

	public static IPackageManager getPM() {
		return ActivityThread.getPackageManager();
	}

	@Override
	protected HookObject<IPackageManager> initHookObject() {
		return new HookObject<IPackageManager>(getPM());
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new ResultStaticHook("addPermissionAsync", true));
		addHook(new ResultStaticHook("addPermission", true));
	}

	@Override
	public void inject() throws Throwable {

		// NOTE:所有进程共享一个包管理器
		Field fPM = ActivityThread.class.getDeclaredField("sPackageManager");
		fPM.setAccessible(true);
		IHookObject<IPackageManager> hookedPMObject = getHookObject();
		final IPackageManager hookedPM = hookedPMObject.getProxyObject();
		fPM.set(null, hookedPM);
		final IBinder baseBinder = hookedPM.asBinder();

		HookBinder<IPackageManager> pmHookBinder = new HookBinder<IPackageManager>() {
			@Override
			protected IBinder queryBaseBinder() {
				return baseBinder;
			}

			@Override
			protected IPackageManager createInterface(IBinder baseBinder) {
				return hookedPM;
			}
		};
		pmHookBinder.injectService("package");

	}

	@Override
	public boolean isEnvBad() {
		return getHookObject().getProxyObject() != getPM();
	}
}
