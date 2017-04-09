package com.lody.virtual.client.hook.proxies.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.text.TextUtils;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.delegate.PhoneInfoDelegate;

import java.lang.reflect.Method;

import mirror.android.net.wifi.IWifiManager;

/**
 * @author Lody
 * @see android.net.wifi.WifiManager
 */
@SuppressLint("HardwareIds")
@Inject(MethodProxies.class)
public class WifiManagerStub extends BinderInvocationProxy {
    public WifiManagerStub() {
        super(IWifiManager.Stub.asInterface, Context.WIFI_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new GetConnectionInfo());
    }

    private static class GetConnectionInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getConnectionInfo";
        }

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
    }
}
