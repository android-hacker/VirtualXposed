package com.lc.interceptor.service.providers;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.lc.interceptor.proto.NetworkInfoMirror;
import com.lc.interceptor.service.providers.base.InterceptorDataProvider;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.connectivity.ConnectivityPatch;

/**
 * @author legency
 */
public class ConnectivityProvider extends InterceptorDataProvider {

    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return ConnectivityPatch.class;
    }

    /**
     * @return
     * @see com.lc.interceptor.client.hook.patch.interceptor.connectivity.Interceptor_GetActiveNetworkInfo
     */
    private NetworkInfo getActiveNetworkInfo() {
        return new NetworkInfoMirror.Builder().setNetworkType(ConnectivityManager.TYPE_WIFI).setSubtype(0)
                .setTypeName("WIFI").setAvailable(true).
                        setDetailedState(NetworkInfo.DetailedState.CONNECTED).
                        setExtraInfo("wifi_test_name").create();
    }

}
