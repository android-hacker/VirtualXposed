package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.net.wifi.IWifiManager;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class WifiBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.WIFI_SERVICE);
		return IWifiManager.Stub.asInterface.call(binder);
	}
}
