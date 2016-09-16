package com.lody.virtual.client.hook.patchs.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.binders.WifiBinderDelegate;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Method;

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
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new StaticHook("getConnectionInfo") {
			@Override
			public Object call(Object who, Method method, Object... args) throws Throwable {
				WifiInfo info = (WifiInfo) super.call(who, method, args);
				if (info != null) {
					if (info.getMacAddress().startsWith("00-00-00-00-00-00")) {
						Reflect.on(info).set("mMacAddress", "00:00:08:76:54:32");
					}

				}
				return info;
			}
		});
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.WIFI_SERVICE) != getHookDelegate();
	}
}
