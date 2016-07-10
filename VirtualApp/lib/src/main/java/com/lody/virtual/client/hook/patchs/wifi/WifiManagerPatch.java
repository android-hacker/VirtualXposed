package com.lody.virtual.client.hook.patchs.wifi;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookWifiBinder;

import android.content.Context;
import android.net.wifi.IWifiManager;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 * @see IWifiManager
 * @see android.net.wifi.WifiManager
 */
@Patch({Hook_GetBatchedScanResults.class, Hook_GetScanResults.class, Hook_SetWifiEnabled.class})
public class WifiManagerPatch extends PatchObject<IWifiManager, HookWifiBinder> {
	@Override
	protected HookWifiBinder initHookObject() {
		return new HookWifiBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.WIFI_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.WIFI_SERVICE) != getHookObject();
	}
}
