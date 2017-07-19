package com.lody.virtual.client.hook.proxies.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.stub.VASettings;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import mirror.android.net.wifi.IWifiManager;
import mirror.android.net.wifi.WifiSsid;

/**
 * @author Lody
 * @see android.net.wifi.WifiManager
 */
@SuppressLint("HardwareIds")
@Inject(MethodProxies.class)
public class WifiManagerStub extends BinderInvocationProxy {

    public static class IPInfo {
        NetworkInterface intf;
        InetAddress addr;
        String ip;
        int ip_hex;
        int netmask_hex;
    }

    public WifiManagerStub() {
        super(IWifiManager.Stub.asInterface, Context.WIFI_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "isWifiEnabled";
            }

            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                if (VASettings.Wifi.FAKE_WIFI_STATE) {
                    return true;
                }
                return super.call(who, method, args);
            }
        });
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "getWifiEnabledState";
            }

            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                if (VASettings.Wifi.FAKE_WIFI_STATE) {
                    return WifiManager.WIFI_STATE_ENABLED;
                }
                return super.call(who, method, args);
            }
        });
        addMethodProxy(new GetConnectionInfo());
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "createDhcpInfo";
            }

            @Override
            public Object call(Object who, Method method, Object... args) throws Throwable {
                if (VASettings.Wifi.FAKE_WIFI_STATE) {
                    IPInfo ipInfo = getIPInfo();
                    if (ipInfo != null) {
                        return createDhcpInfo(ipInfo);
                    }
                }
                return super.call(who, method, args);
            }
        });
    }


    private static class GetConnectionInfo extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getConnectionInfo";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            WifiInfo info = (WifiInfo) method.invoke(who, args);
            if (VASettings.Wifi.FAKE_WIFI_STATE) {
                return createWifiInfo();
            }
            return info;
        }
    }


    // get current ip and netmask
    public static IPInfo getIPInfo() {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = isIPv4Address(sAddr);
                        if (isIPv4) {
                            IPInfo info = new IPInfo();
                            info.addr = addr;
                            info.intf = intf;
                            info.ip = sAddr;
                            info.ip_hex = InetAddress_to_hex(addr);
                            info.netmask_hex = netmask_to_hex(intf.getInterfaceAddresses().get(0).getNetworkPrefixLength());
                            return info;
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean isIPv4Address(String input) {
        Pattern IPV4_PATTERN = Pattern.compile("^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$");
        return IPV4_PATTERN.matcher(input).matches();
    }

    private static int netmask_to_hex(int netmask_slash) {
        int r = 0;
        int b = 1;
        for (int i = 0; i < netmask_slash; i++, b = b << 1)
            r |= b;
        return r;
    }

    private static int InetAddress_to_hex(InetAddress a) {
        int result = 0;
        byte b[] = a.getAddress();
        for (int i = 0; i < 4; i++)
            result |= (b[i] & 0xff) << (8 * i);
        return result;
    }

    public DhcpInfo createDhcpInfo(IPInfo ip) {
        DhcpInfo i = new DhcpInfo();
        i.ipAddress = ip.ip_hex;
        i.netmask = ip.netmask_hex;
        i.dns1 = 0x04040404;
        i.dns2 = 0x08080808;
        return i;
    }

    public static WifiInfo createWifiInfo() throws Exception {
        WifiInfo info = mirror.android.net.wifi.WifiInfo.ctor.newInstance();
        IPInfo ip = getIPInfo();
        InetAddress address = (ip != null ? ip.addr : null);
        mirror.android.net.wifi.WifiInfo.mNetworkId.set(info, 1);
        mirror.android.net.wifi.WifiInfo.mSupplicantState.set(info, SupplicantState.COMPLETED);
        mirror.android.net.wifi.WifiInfo.mBSSID.set(info, VASettings.Wifi.BSSID);
        mirror.android.net.wifi.WifiInfo.mMacAddress.set(info, VASettings.Wifi.MAC);
        mirror.android.net.wifi.WifiInfo.mIpAddress.set(info, address);
        mirror.android.net.wifi.WifiInfo.mLinkSpeed.set(info, 65);
        if (Build.VERSION.SDK_INT >= 21) {
            mirror.android.net.wifi.WifiInfo.mFrequency.set(info, 5000); // MHz
        }
        mirror.android.net.wifi.WifiInfo.mRssi.set(info, 200); // MAX_RSSI
        if (mirror.android.net.wifi.WifiInfo.mWifiSsid != null) {
            mirror.android.net.wifi.WifiInfo.mWifiSsid.set(info, WifiSsid.createFromAsciiEncoded.call(VASettings.Wifi.SSID));
        } else {
            mirror.android.net.wifi.WifiInfo.mSSID.set(info, VASettings.Wifi.SSID);
        }
        return info;
    }
}
