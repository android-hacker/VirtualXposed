package com.lody.virtual.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.NetworkInfo;
import android.os.RemoteException;

import com.lody.virtual.IConnectivityManager;
import com.lody.virtual.client.stub.VASettings;
import com.lody.virtual.helper.utils.ArrayUtils;

import mirror.android.os.ServiceManager;


/**
 * @author Lody
 */

public class VConnectivityManagerService extends IConnectivityManager.Stub {

    private static final VConnectivityManagerService sService = new VConnectivityManagerService();

    private android.net.IConnectivityManager mService;


    private VConnectivityManagerService() {
        mService = android.net.IConnectivityManager.Stub.asInterface(
                ServiceManager.getService.call(Context.CONNECTIVITY_SERVICE)
        );
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() throws RemoteException {
        NetworkInfo info = mService.getActiveNetworkInfo();
        if (VASettings.Wifi.FAKE_WIFI_STATE) {
            if (info == null || (info.getType() != ConnectivityManager.TYPE_WIFI)) {
                return getFakeWifiNetworkInfo();
            }
        }
        return info;
    }


    @Override
    public NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked) throws RemoteException {
        NetworkInfo info = mService.getActiveNetworkInfoForUid(uid, ignoreBlocked);
        if (VASettings.Wifi.FAKE_WIFI_STATE) {
            if (info == null || (info.getType() != ConnectivityManager.TYPE_WIFI)) {
                return getFakeWifiNetworkInfo();
            }
        }
        return info;
    }

    @Override
    public NetworkInfo getNetworkInfo(int networkType) throws RemoteException {
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            if (VASettings.Wifi.FAKE_WIFI_STATE) {
                return getFakeWifiNetworkInfo();
            }

        }
        return mService.getNetworkInfo(networkType);
    }

    @Override
    public NetworkInfo[] getAllNetworkInfo() throws RemoteException {
        NetworkInfo[] infos = mService.getAllNetworkInfo();
        if (VASettings.Wifi.FAKE_WIFI_STATE) {
            int wifiNetworkIdx;
            boolean wifi_found = false;
            for (wifiNetworkIdx = 0; wifiNetworkIdx < infos.length; wifiNetworkIdx++)
                if (infos[wifiNetworkIdx].getType() == ConnectivityManager.TYPE_WIFI) {
                    wifi_found = true;
                    break;
                }

            if (wifi_found && infos[wifiNetworkIdx].isConnected()) {
                // if we're already on wifi don't interfere.
                return infos;
            }
            if (wifi_found) {
                infos[wifiNetworkIdx] = getFakeWifiNetworkInfo();
            } else {
                infos = (NetworkInfo[]) ArrayUtils.push(infos, getFakeWifiNetworkInfo());
            }
        }
        return infos;
    }

    @Override
    public boolean isActiveNetworkMetered() throws RemoteException {
        if (VASettings.Wifi.FAKE_WIFI_STATE) {
            return false;
        }
        return mService.isActiveNetworkMetered();
    }

    @Override
    public boolean requestRouteToHostAddress(int networkType, int address) throws RemoteException {
        return mService.requestRouteToHostAddress(networkType, address);
    }

    @Override
    public LinkProperties getActiveLinkProperties() throws RemoteException {
        return mService.getActiveLinkProperties();
    }

    @Override
    public LinkProperties getLinkProperties(int networkType) throws RemoteException {
        return mService.getLinkProperties(networkType);
    }


    public NetworkInfo getFakeWifiNetworkInfo() {
        return createNetworkInfo(ConnectivityManager.TYPE_WIFI, "WIFI");
    }

    private NetworkInfo createNetworkInfo(final int networkType, String typeName) {
        NetworkInfo networkInfo;
        if (mirror.android.net.NetworkInfo.ctor != null) {
            networkInfo = mirror.android.net.NetworkInfo.ctor.newInstance(0, 0, null, null);
        } else {
            networkInfo = mirror.android.net.NetworkInfo.ctorOld.newInstance(0);
        }
        mirror.android.net.NetworkInfo.mNetworkType.set(networkInfo, networkType);
        mirror.android.net.NetworkInfo.mIsAvailable.set(networkInfo, true);
        mirror.android.net.NetworkInfo.mTypeName.set(networkInfo, typeName);
        mirror.android.net.NetworkInfo.mState.set(networkInfo, NetworkInfo.State.CONNECTED);
        mirror.android.net.NetworkInfo.mDetailedState.set(networkInfo, NetworkInfo.DetailedState.CONNECTED);
        return networkInfo;
    }


    public static VConnectivityManagerService get() {
        return sService;
    }
}
