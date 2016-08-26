package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.IPowerManager;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
public class PowerBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.POWER_SERVICE);
		return IPowerManager.Stub.asInterface.call(binder);
	}
}
