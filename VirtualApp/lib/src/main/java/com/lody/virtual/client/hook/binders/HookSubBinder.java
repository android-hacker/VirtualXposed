package com.lody.virtual.client.hook.binders;

import com.android.internal.telephony.ISub;
import com.lody.virtual.client.hook.base.HookBinder;

import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 */

public class HookSubBinder extends HookBinder<ISub> {

	public static final String SERVICE_NAME = "isub";
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(SERVICE_NAME);
	}

	@Override
	protected ISub createInterface(IBinder baseBinder) {
		return ISub.Stub.asInterface(baseBinder);
	}
}
