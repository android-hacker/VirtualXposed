package com.lc.interceptor.client.hook.patch.interceptor.connectivity;

import com.lc.interceptor.client.hook.base.InterceptorServiceHook;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.connectivity.ConnectivityPatch;

/**
 * @author Junelegency
 *
 */
public class Interceptor_GetActiveNetworkInfo extends InterceptorServiceHook {

    @Override
    public String getName() {
        return "getActiveNetworkInfo";
    }

    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return ConnectivityPatch.class;
    }
}
