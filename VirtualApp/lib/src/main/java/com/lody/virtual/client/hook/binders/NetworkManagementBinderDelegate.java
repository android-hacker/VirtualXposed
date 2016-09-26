package com.lody.virtual.client.hook.binders;

import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.INetworkManagementService;
import mirror.android.os.ServiceManager;

public class NetworkManagementBinderDelegate extends HookBinderDelegate {

	public static final String NETWORKMANAGEMENT_SERVICE = "network_management";

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(NETWORKMANAGEMENT_SERVICE);
		return INetworkManagementService.Stub.asInterface.call(binder);
	}
}
