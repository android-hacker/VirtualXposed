package com.lc.interceptor.client.hook.patch.interceptor.wifi;

import com.lc.interceptor.client.hook.base.InterceptorServiceHook;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.wifi.WifiManagerPatch;

/**
 * Created by lichen:) on 2016/9/1.
 */
public abstract class BaseInterceptorWifi extends InterceptorServiceHook {
    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return WifiManagerPatch.class;
    }
}
