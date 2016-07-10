package com.lody.virtual.client.hook.binders;

import com.android.internal.telephony.IPhoneSubInfo;
import com.lody.virtual.client.hook.base.HookBinder;

import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookPhoneSubInfoBinder extends HookBinder<IPhoneSubInfo> {
	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService("iphonesubinfo");
	}

	@Override
	protected IPhoneSubInfo createInterface(IBinder baseBinder) {
		return IPhoneSubInfo.Stub.asInterface(baseBinder);
	}
}
