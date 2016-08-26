package com.lody.virtual.client.hook.patchs.wifi;

import android.content.Context;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.WifiBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see android.net.wifi.WifiManager
 */
@Patch({GetBatchedScanResults.class, GetScanResults.class, SetWifiEnabled.class})
public class WifiManagerPatch extends PatchDelegate<WifiBinderDelegate> {
	@Override
	protected WifiBinderDelegate createHookDelegate() {
		return new WifiBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.WIFI_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.WIFI_SERVICE) != getHookDelegate();
	}
}
