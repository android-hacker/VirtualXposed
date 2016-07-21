package com.lody.virtual.client.hook.patchs.imms;

import android.os.ServiceManager;

import com.android.internal.telephony.IMms;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookIMMSBinder;

/**
 * @author Lody
 *
 */
public class MmsPatch extends PatchObject<IMms, HookIMMSBinder> {
	@Override
	protected HookIMMSBinder initHookObject() {
		return new HookIMMSBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(HookIMMSBinder.SERVICE_NAME);
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(HookIMMSBinder.SERVICE_NAME);
	}
}
