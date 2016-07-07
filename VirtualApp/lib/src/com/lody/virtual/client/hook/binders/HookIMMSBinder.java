package com.lody.virtual.client.hook.binders;

import com.android.internal.telephony.IMms;
import com.lody.virtual.client.hook.base.HookBinder;

import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookIMMSBinder extends HookBinder<IMms> {

	public static final String SERVICE_NAME = "imms";
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(SERVICE_NAME);
	}

	@Override
	protected IMms createInterface(IBinder baseBinder) {
		return IMms.Stub.asInterface(baseBinder);
	}
}
