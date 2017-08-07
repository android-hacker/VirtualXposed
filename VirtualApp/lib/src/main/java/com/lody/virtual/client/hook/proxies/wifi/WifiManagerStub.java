package com.lody.virtual.client.hook.proxies.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.WorkSource;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.client.ipc.VirtualLocationManager;
import com.lody.virtual.helper.utils.ArrayUtils;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.marks.FakeDeviceMark;
import com.lody.virtual.helper.utils.marks.FakeLocMark;
import com.lody.virtual.remote.vloc.VWifi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
        addMethodProxy(new GetScanResults());
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
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "getWifiEnabledState";
            }

            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                if (isFakeLocationEnable()) {
                    return WifiManager.WIFI_STATE_DISABLED;
                }
                return super.call(who, method, args);
            }
        });
    }

    @FakeLocMark("Fake wifi bssid")
    @FakeDeviceMark("fake wifi MAC")
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

    @FakeLocMark("fake scan result")
    private final class GetScanResults extends ReplaceCallingPkgMethodProxy {

        public GetScanResults() {
            super("getScanResults");
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
//            noinspection unchecked
            if (isFakeLocationEnable()) {
                new ArrayList<ScanResult>(0);
            }
            return super.call(who, method, args);
        }
    }

    private static ScanResult cloneScanResult(Parcelable scanResult) {
        Parcel p = Parcel.obtain();
        scanResult.writeToParcel(p, 0);
        p.setDataPosition(0);
        ScanResult newScanResult = Reflect.on(scanResult).field("CREATOR").call("createFromParcel", p).get();
        p.recycle();
        return newScanResult;
    }


}
