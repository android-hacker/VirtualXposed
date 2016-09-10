package com.lc.interceptor.service.providers;

import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.lc.interceptor.proto.WifiInfoMirror;
import com.lc.interceptor.service.providers.base.InterceptorDataProvider;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.wifi.WifiManagerPatch;
import com.lody.virtual.helper.utils.Reflect;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * @author legency
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
        String hostName = null;
        byte[] ipAddress = {100, 84, -55, -103};
        int family = 2;
        InetAddress inetAddress = WifiInfoMirror.InetAddressL.ctor.newInstance(family, ipAddress, hostName);
        return new WifiInfoMirror.Builder().setFrequency(5825).setMacAddress("02:00:00:00:00:00")
                .setBSSID("00:00:00:00:00:00").setEphemeral(false).setLinkSpeed(72)
                .setMeteredHint(false).setNetworkId(7).setRssi(-55)
                .setSupplicantState(SupplicantState.COMPLETED)
                .setIpAddress(inetAddress).setWifiSsid("test_ssid").create();

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
