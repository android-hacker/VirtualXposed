package com.lc.interceptor.service.providers;

import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.lc.interceptor.service.providers.base.InterceptorDataProvider;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.wifi.WifiManagerPatch;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Constructor;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lichen:) on 2016/9/9.
 */
public class WifiManagerProvider extends InterceptorDataProvider {

    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return WifiManagerPatch.class;
    }

    /**
     * @return
     * @see com.lc.interceptor.client.hook.patch.interceptor.wifi.Interceptor_GetConnectionInfo
     */
    private WifiInfo getConnectionInfo() {
        return createFakeWifi();
    }

    /**
     * @param callingPackage
     * @return
     * @see com.lc.interceptor.client.hook.patch.interceptor.wifi.Interceptor_GetScanResults
     */
    List<ScanResult> getScanResults(String callingPackage) {
        return createScanResult();
    }

    /**
     * @return
     * @see com.lc.interceptor.client.hook.patch.interceptor.wifi.Interceptor_GetWifiEnabledState
     */
    public int getWifiEnabledState() {
        return WifiManager.WIFI_STATE_ENABLED;
    }


    private WifiInfo createFakeWifi() {
        WifiInfo wifi = Reflect.on(WifiInfo.class).create().get();
        Reflect.on(wifi).call("setFrequency", 5825);
        Reflect.on(wifi).call("setMacAddress", "02:00:00:00:00:00");
        Reflect.on(wifi).call("setBSSID", "00:00:00:00:00:00");
        try {
            Reflect.on(wifi).call("setEphemeral", false);
        } catch (Throwable e) {
        }
        Reflect.on(wifi).call("setLinkSpeed", 72);
        Reflect.on(wifi).call("setMeteredHint", false);
        Reflect.on(wifi).call("setMeteredHint", false);
        Reflect.on(wifi).call("setNetworkId", 7);
        Reflect.on(wifi).call("setRssi", -55);


        SupplicantState supplicantState = SupplicantState.COMPLETED;
        Reflect.on(wifi).call("setSupplicantState", supplicantState);

        String hostName = null;
        byte[] ipAddress = {100, 84, -55, -103};
        int family = 2;
        InetAddress i = null;
        try {
            Class<InetAddress> clazz = InetAddress.class;
            Constructor<?>[] cs = clazz.getDeclaredConstructors();
            Constructor<?> c0 = cs[0];
            c0.setAccessible(true);
            i = (InetAddress) c0.newInstance(family, ipAddress, hostName);

        } catch (Throwable e) {
            e.printStackTrace();
        }
        Reflect.on(wifi).call("setInetAddress", i);
        Reflect.on(wifi).call("setSSID", Reflect.on("android.net.wifi.WifiSsid").call("createFromAsciiEncoded", "test_ssid").get());
        return wifi;
    }


    //
    String[] BSSIDS = {"00:00:00:00:00:00"};

    private List createScanResult() {
        List<ScanResult> scanResults = new ArrayList<>();
        for (String BSSID : BSSIDS) {
            ScanResult scanResult = Reflect.on(ScanResult.class).create().get();
            scanResult.SSID = "test_ssid";
            scanResult.BSSID = BSSID;
            scanResults.add(scanResult);
        }
        return scanResults;
    }


}
