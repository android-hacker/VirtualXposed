package android.net;

import android.net.NetworkInfo;
import android.net.LinkProperties;

interface IConnectivityManager {

    NetworkInfo getActiveNetworkInfo();
    NetworkInfo getActiveNetworkInfoForUid(int uid, boolean ignoreBlocked);

    NetworkInfo getNetworkInfo(int networkType);
    NetworkInfo[] getAllNetworkInfo();
    boolean isActiveNetworkMetered();
    boolean requestRouteToHostAddress(int networkType, int address);
    LinkProperties getActiveLinkProperties();
    LinkProperties getLinkProperties(int networkType);

}