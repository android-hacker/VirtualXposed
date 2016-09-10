package com.lc.interceptor.client.hook.patch.interceptor.location;


import com.lc.interceptor.client.hook.base.InterceptorServiceHook;
import com.lc.interceptor.client.hook.patch.interceptor.LocationAMAPHack;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.location.LocationManagerPatch;

import java.lang.reflect.Method;

/**
 * @author Junelegency
 *
 */
public class Interceptor_RequestLocationUpdates extends InterceptorServiceHook {


    @Override
    public String getName() {
        return "requestLocationUpdates";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        return super.call(who, method, args);
    }

    @Override
    public boolean isOnHookConsumed() {
        return true;
    }

    @Override
    public boolean isOnHookEnabled() {
        return true;
    }

    @Override
    public boolean isEnable() {
        return LocationAMAPHack.LOCATION_MOCK_GPS;
    }

    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return LocationManagerPatch.class;
    }
}
