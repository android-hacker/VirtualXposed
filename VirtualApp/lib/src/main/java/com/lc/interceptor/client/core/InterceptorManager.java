package com.lc.interceptor.client.core;

import com.lc.interceptor.client.hook.base.InterceptorHook;
import com.lc.interceptor.client.hook.patch.interceptor.connectivity.Interceptor_GetActiveNetworkInfo;
import com.lc.interceptor.client.hook.patch.interceptor.location.Interceptor_RemoveUpdates;
import com.lc.interceptor.client.hook.patch.interceptor.location.Interceptor_RequestLocationUpdates;
import com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetActivePhoneTypeForSubscriber;
import com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetAllCellInfo;
import com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetAllCellInfoUsingSubId;
import com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetCellLocation;
import com.lc.interceptor.client.hook.patch.interceptor.telephony.Interceptor_GetNeighboringCellInfo;
import com.lc.interceptor.client.hook.patch.interceptor.wifi.Interceptor_GetConnectionInfo;
import com.lc.interceptor.client.hook.patch.interceptor.wifi.Interceptor_GetScanResults;
import com.lc.interceptor.client.hook.patch.interceptor.wifi.Interceptor_GetWifiEnabledState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lichen:) on 2016/9/1.
 */
public class InterceptorManager {

    static List<InterceptorHook> interceptorHooks = new ArrayList<>();

    static {
        // connectivity interceptor
        interceptorHooks.add(new Interceptor_GetActiveNetworkInfo());
        //location interceptor
        interceptorHooks.add(new Interceptor_RemoveUpdates());
        interceptorHooks.add(new Interceptor_RequestLocationUpdates());

        // telephony interceptor
        interceptorHooks.add(new Interceptor_GetActivePhoneTypeForSubscriber());
        interceptorHooks.add(new Interceptor_GetAllCellInfo());
        interceptorHooks.add(new Interceptor_GetAllCellInfoUsingSubId());
        interceptorHooks.add(new Interceptor_GetCellLocation());
        interceptorHooks.add(new Interceptor_GetNeighboringCellInfo());

        // wifi interceptor
        interceptorHooks.add(new Interceptor_GetConnectionInfo());
        interceptorHooks.add(new Interceptor_GetScanResults());
        interceptorHooks.add(new Interceptor_GetWifiEnabledState());
    }

    public static List<InterceptorHook> getInterceptors() {
        return interceptorHooks;
    }

    /**
     *  TODO 过滤 自定义配置 进程 包名  指定场景下的 拦截器
     * @return
     */
    public List<InterceptorHook> getInterceptorsByFilter() {
        return null;
    }
}
