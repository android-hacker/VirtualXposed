package com.lody.virtual.client.hook.binders;

import com.android.internal.telephony.ITelephony;
import com.lody.virtual.client.hook.base.HookBinder;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookTelephonyBinder extends HookBinder<ITelephony> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.TELEPHONY_SERVICE);
	}

	@Override
	protected ITelephony createInterface(IBinder baseBinder) {
		return ITelephony.Stub.asInterface(baseBinder);
	}
}
