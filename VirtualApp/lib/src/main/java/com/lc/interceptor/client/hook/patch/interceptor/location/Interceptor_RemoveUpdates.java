package com.lc.interceptor.client.hook.patch.interceptor.location;


import com.lc.interceptor.client.hook.base.InterceptorHook;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.patchs.location.LocationManagerPatch;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * Created by legency on 2016/8/21.
 */
public class Interceptor_RemoveUpdates extends InterceptorHook {

    @Override
    public boolean beforeCall(Object who, Method method, Object... args) {
        HookUtils.replaceFirstAppPkg(args);
        return super.beforeCall(who, method, args);
    }

    @Override
    public String getName() {
        return "removeUpdates";
    }

    @Override
    public boolean isOnHookConsumed() {
        return true;
    }

    @Override
    public Class<? extends PatchDelegate> getDelegatePatch() {
        return LocationManagerPatch.class;
    }
}
