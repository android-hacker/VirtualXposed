package com.lody.virtual.client.hook.patchs.pm;

import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;
import com.lody.virtual.client.hook.base.HookDelegate;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;

import mirror.android.app.ActivityThread;

/**
 * @author Lody
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
public final class PackageManagerPatch extends PatchDelegate<HookDelegate<IInterface>> {

	public PackageManagerPatch() {
		super(new HookDelegate<IInterface>(ActivityThread.sPackageManager.get()));
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new ResultStaticHook("addPermissionAsync", true));
		addHook(new ResultStaticHook("addPermission", true));
	}

	@Override
	public void inject() throws Throwable {
		final IInterface hookedPM = getHookDelegate().getProxyInterface();
		ActivityThread.sPackageManager.set(hookedPM);

		HookBinderDelegate pmHookBinder = new HookBinderDelegate(getHookDelegate().getBaseInterface());
		pmHookBinder.copyHooks(getHookDelegate());
		pmHookBinder.replaceService("package");
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate().getProxyInterface() != ActivityThread.sPackageManager.get();
	}
}
