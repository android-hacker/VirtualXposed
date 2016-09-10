package com.lc.interceptor.service.providers.base;

import com.lody.virtual.client.hook.base.PatchDelegate;

/**
 * Created by lichen:) on 2016/9/9.
 */
public abstract class InterceptorDataProvider {
    abstract public Class<? extends PatchDelegate> getDelegatePatch();

}
