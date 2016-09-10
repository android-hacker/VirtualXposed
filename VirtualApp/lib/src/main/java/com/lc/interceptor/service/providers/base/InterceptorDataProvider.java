package com.lc.interceptor.service.providers.base;

import com.lody.virtual.client.hook.base.PatchDelegate;

/**
 * @author legency
 */
public abstract class InterceptorDataProvider {
    abstract public Class<? extends PatchDelegate> getDelegatePatch();

}
