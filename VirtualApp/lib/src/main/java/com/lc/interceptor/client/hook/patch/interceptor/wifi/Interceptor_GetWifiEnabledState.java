package com.lc.interceptor.client.hook.patch.interceptor.wifi;

import android.net.wifi.WifiManager;

import java.lang.reflect.Method;

/**
 * Created by legency on 2016/8/21.
 */
public class Interceptor_GetWifiEnabledState extends BaseInterceptorWifi {

    @Override
    public String getName() {
        return "getWifiEnabledState";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        return WifiManager.WIFI_STATE_ENABLED;
    }


}
