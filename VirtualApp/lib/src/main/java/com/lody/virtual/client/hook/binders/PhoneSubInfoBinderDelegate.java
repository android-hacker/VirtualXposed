package com.lody.virtual.client.hook.binders;

import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.telephony.IPhoneSubInfo;

/**
 * @author Lody
 *
 */
public class PhoneSubInfoBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call("iphonesubinfo");
		return IPhoneSubInfo.Stub.asInterface.call(binder);
	}
}
