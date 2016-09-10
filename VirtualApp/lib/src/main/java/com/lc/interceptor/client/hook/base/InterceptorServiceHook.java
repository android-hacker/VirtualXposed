package com.lc.interceptor.client.hook.base;

import com.lc.interceptor.client.local.interceptor.VInterceptorCallManager;

import java.lang.reflect.Method;

/**
 * @author Junelegency
 *
 */
public abstract class InterceptorServiceHook extends InterceptorHook{
    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        return VInterceptorCallManager.get().call(this,args);
    }
}
