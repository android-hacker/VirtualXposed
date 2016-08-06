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
@Patch({Hook_GetPackageInfo.class, Hook_GetApplicationInfo.class, Hook_GetActivityInfo.class, Hook_GetServiceInfo.class,
		Hook_GetPermissions.class, Hook_GetProviderInfo.class, Hook_GetReceiverInfo.class,
		Hook_GetPermissionFlags.class,

		Hook_CheckSignatures.class,

		Hook_ResolveIntent.class, Hook_ResolveService.class, Hook_ResolveContentProvider.class,

		Hook_QueryIntentActivities.class, Hook_QueryIntentServices.class, Hook_QueryIntentReceivers.class,
		Hook_QueryIntentContentProviders.class, Hook_QueryContentProviders.class,

		Hook_CheckPermission.class, Hook_RevokeRuntimePermission.class,

		Hook_SetPackageStoppedState.class, Hook_GetInstalledPackages.class, Hook_GetInstalledApplications.class,
		Hook_SetApplicationEnabledSetting.class, Hook_GetApplicationEnabledSetting.class,
		Hook_SetApplicationBlockedSettingAsUser.class, Hook_GetApplicationBlockedSettingAsUser.class,
		Hook_DeleteApplicationCacheFiles.class, Hook_GetInstallerPackageName.class,

		Hook_IsPackageAvailable.class, Hook_IsPackageForzen.class, Hook_GetComponentEnabledSetting.class,
		Hook_SetComponentEnabledSetting.class,

		Hook_GetPackageUid.class, Hook_GetPackageGids.class, Hook_GetPackagesForUid.class,

		Hook_AddPackageToPreferred.class, Hook_RemovePackageFromPreferred.class,
		Hook_ClearPackagePreferredActivities.class, Hook_GetPreferredActivities.class,
		Hook_ClearPackagePersistentPreferredActivities.class, Hook_GetPermissionGroupInfo.class,
		Hook_DeletePackage.class, Hook_GetPackageInstaller.class, Hook_AddOnPermissionsChangeListener.class,
		Hook_RemoveOnPermissionsChangeListener.class, Hook_ActivitySupportsIntent.class,

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
