package com.lody.virtual.client.hook.patchs.appops;

import com.android.internal.app.IAppOpsService;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookAppOpsBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 *
 *         Fuck the AppOpsService.
 *
 * @see android.app.AppOpsManager
 * @see IAppOpsService
 */
@Patch({Hook_CheckAudioOperation.class, Hook_CheckOperation.class, Hook_CheckPackage.class, Hook_FinishOperation.class,
		Hook_GetOpsForPackage.class, Hook_NoteOperation.class, Hook_SetMode.class, Hook_StartOperation.class,
		Hook_StartWatchingMode.class, Hook_NoteProxyOperation.class, Hook_ResetAllModes.class,})
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AppOpsManagerPatch extends PatchObject<IAppOpsService, HookAppOpsBinder> {

	@Override
	protected HookAppOpsBinder initHookObject() {
		return new HookAppOpsBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.APP_OPS_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.APP_OPS_SERVICE) != getHookObject();
	}

}
