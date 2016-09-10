package com.lc.interceptor.client.hook.patch.interceptor.telephony;

import com.lc.interceptor.client.hook.base.InterceptorHook;
import com.lc.interceptor.client.hook.base.InterceptorServiceHook;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.telephony.TelephonyPatch;

/**
 * Created by lichen:) on 2016/9/1.
 */
public abstract class BaseInterceptorTelephony extends InterceptorServiceHook{
    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return TelephonyPatch.class;
    }
}
