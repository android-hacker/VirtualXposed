package com.lody.virtual.client.hook.patchs.telephony;

import com.android.internal.telephony.ITelephony;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookTelephonyBinder;

import android.content.Context;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see ITelephony
 */
@Patch({Hook_GetDeviceId.class, Hook_GetCellLocation.class, Hook_GetLine1NumberForDisplay.class})
public class TelephonyPatch extends PatchObject<ITelephony, HookTelephonyBinder> {

	@Override
	protected HookTelephonyBinder initHookObject() {
		return new HookTelephonyBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.TELEPHONY_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.TELEPHONY_SERVICE);
	}
}
