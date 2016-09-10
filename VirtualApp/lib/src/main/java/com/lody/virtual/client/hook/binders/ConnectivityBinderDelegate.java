package com.lody.virtual.client.hook.binders;


import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.content.ClipboardManager;
import mirror.android.location.ILocationManager;
import mirror.android.net.IConnectivityManager;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class ConnectivityBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.CONNECTIVITY_SERVICE);
		return IConnectivityManager.Stub.asInterface.call(binder);
	}
}
