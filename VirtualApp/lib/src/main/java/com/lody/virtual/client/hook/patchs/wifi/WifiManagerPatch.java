package com.lody.virtual.client.hook.patchs.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Method;

import mirror.android.net.wifi.IWifiManager;

/**
 * @author Lody
 *
 * @see android.net.wifi.WifiManager
 */
@Patch({GetBatchedScanResults.class, GetScanResults.class, SetWifiEnabled.class})
public class WifiManagerPatch extends PatchBinderDelegate {
	public WifiManagerPatch() {
		super(IWifiManager.Stub.TYPE, Context.WIFI_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new StaticHook("getConnectionInfo") {
			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				WifiInfo info = (WifiInfo) super.call(who, method, args);
				if (info != null && info.getMacAddress()!=null) {
					String address = VirtualCore.get().getPhoneInfoDelegate().getMacAddress(info.getMacAddress(), getVUserId());
					Reflect.on(info).set("mMacAddress", address);
				}
				return info;
			}
		});
	}
}
