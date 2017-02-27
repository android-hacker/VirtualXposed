package com.lody.virtual.client.hook.patchs.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;

import java.lang.reflect.Method;

import mirror.android.net.wifi.IWifiManager;

/**
 * @author Lody
 * @see android.net.wifi.WifiManager
 */
@SuppressLint("HardwareIds")
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
                WifiInfo info = (WifiInfo) method.invoke(who, args);
                PhoneInfoDelegate delegate = VirtualCore.get().getPhoneInfoDelegate();
                if (info != null && delegate != null) {
                    String oldMac = info.getMacAddress();
                    if (oldMac != null) {
                        String newMac = delegate.getMacAddress(oldMac, getAppUserId());
                        if (!TextUtils.equals(oldMac, newMac)) {
                            mirror.android.net.wifi.WifiInfo.mMacAddress.set(info, newMac);
                        }
                    }
                }
                return info;
            }
        });
    }
}
