package com.lody.virtual.client.hook.patchs.mount;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookMountServiceBinder;

import android.os.ServiceManager;
import android.os.storage.IMountService;

/**
 * @author Lody
 *
 * @see IMountService
 */
@Patch({Hook_GetVolumeList.class, Hook_Mkdirs.class,})
public class MountServicePatch extends PatchObject<IMountService, HookMountServiceBinder> {

	@Override
	protected HookMountServiceBinder initHookObject() {
		return new HookMountServiceBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(HookMountServiceBinder.SERVICE_NAME);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(HookMountServiceBinder.SERVICE_NAME) != getHookObject();
	}
}
