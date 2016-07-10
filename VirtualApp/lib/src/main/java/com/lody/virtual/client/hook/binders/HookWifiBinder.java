package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.content.Context;
import android.net.wifi.IWifiManager;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
public class HookWifiBinder extends HookBinder<IWifiManager> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.WIFI_SERVICE);
	}

	@Override
	protected IWifiManager createInterface(IBinder baseBinder) {
		return IWifiManager.Stub.asInterface(baseBinder);
	}
}
