package com.lody.virtual.client.hook.patchs.telephony_registry;

import com.android.internal.telephony.ITelephonyRegistry;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookITelephonyRegistryBinder;

import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@Patch({AddOnSubscriptionsChangedListener.class, RemoveOnSubscriptionsChangedListener.class,
		Listen.class, ListenForSubscriber.class})
public class TelephonyRegistryPatch extends PatchObject<ITelephonyRegistry, HookITelephonyRegistryBinder> {

	@Override
	protected HookITelephonyRegistryBinder initHookObject() {
		return new HookITelephonyRegistryBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService("telephony.registry");
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService("telephony.registry");
	}
}
