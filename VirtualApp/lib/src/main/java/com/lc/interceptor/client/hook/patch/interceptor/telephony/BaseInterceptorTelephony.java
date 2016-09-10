package com.lc.interceptor.client.hook.patch.interceptor.telephony;

import com.lc.interceptor.client.hook.base.InterceptorServiceHook;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.telephony.TelephonyPatch;

/**
 * @author Junelegency
 *
 */
public abstract class BaseInterceptorTelephony extends InterceptorServiceHook{
    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return TelephonyPatch.class;
    }
}
