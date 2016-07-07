package com.lody.virtual.client.hook.binders;

import com.android.internal.telephony.ITelephonyRegistry;
import com.lody.virtual.client.hook.base.HookBinder;

import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookITelephonyRegistryBinder extends HookBinder<ITelephonyRegistry> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService("telephony.registry");
	}

	@Override
	protected ITelephonyRegistry createInterface(IBinder baseBinder) {
		return ITelephonyRegistry.Stub.asInterface(baseBinder);
	}
}
