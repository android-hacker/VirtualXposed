package com.lc.interceptor.client.hook.base;

import com.lc.interceptor.client.local.interceptor.VInterceptorCallManager;

import java.lang.reflect.Method;

/**
 * Created by lichen:) on 2016/9/9.
 */
public abstract class InterceptorServiceHook extends InterceptorHook{
    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        return VInterceptorCallManager.get().call(this,args);
    }
}
