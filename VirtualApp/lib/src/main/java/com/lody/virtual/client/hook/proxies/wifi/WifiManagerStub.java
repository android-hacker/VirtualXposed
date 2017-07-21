package com.lody.virtual.client.hook.proxies.wifi;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.WorkSource;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.Mark;

import java.lang.reflect.Method;

import mirror.android.net.wifi.IWifiManager;

/**
 * @author Lody
 * @see android.net.wifi.WifiManager
 */
public class WifiManagerStub extends BinderInvocationProxy {

    private class RemoveWorkSourceMethodProxy extends StaticMethodProxy {

        RemoveWorkSourceMethodProxy(String name) {
            super(name);
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            int index = ArrayUtils.indexOfFirst(args, WorkSource.class);
            if (index >= 0) {
                args[index] = null;
            }
            return super.call(who, method, args);
        }
    }


    public WifiManagerStub() {
        super(IWifiManager.Stub.asInterface, Context.WIFI_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new GetConnectionInfo());
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getScanResults"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getBatchedScanResults"));
        addMethodProxy(new RemoveWorkSourceMethodProxy("acquireWifiLock"));
        addMethodProxy(new RemoveWorkSourceMethodProxy("updateWifiLockWorkSource"));
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            addMethodProxy(new RemoveWorkSourceMethodProxy("startLocationRestrictedScan"));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            addMethodProxy(new RemoveWorkSourceMethodProxy("startScan"));
            addMethodProxy(new RemoveWorkSourceMethodProxy("requestBatchedScan"));
        }
    }

    @Mark("fake wifi MAC")
    private final class GetConnectionInfo extends MethodProxy {
        @Override
        public String getMethodName() {
            return "getConnectionInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            WifiInfo wifiInfo = (WifiInfo) method.invoke(who, args);
            if (wifiInfo != null) {
                mirror.android.net.wifi.WifiInfo.mMacAddress.set(wifiInfo, getDeviceInfo().wifiMac);
            }
            return wifiInfo;
        }
    }
}
