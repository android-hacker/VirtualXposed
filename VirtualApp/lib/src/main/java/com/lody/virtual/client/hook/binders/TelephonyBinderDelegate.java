package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.telephony.ITelephony;

/**
 * @author Lody
 *
 */
public class TelephonyBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.TELEPHONY_SERVICE);
		return ITelephony.Stub.asInterface.call(binder);
	}
}
