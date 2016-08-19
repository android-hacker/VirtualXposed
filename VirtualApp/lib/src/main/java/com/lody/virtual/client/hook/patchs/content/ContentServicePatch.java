package com.lody.virtual.client.hook.patchs.content;

import android.content.IContentService;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookContentBinder;

/**
 * @author Lody
 */

public class ContentServicePatch extends PatchObject<IContentService, HookContentBinder> {
    @Override
    protected HookContentBinder initHookObject() {
        return new HookContentBinder();
    }

    @Override
    protected void applyHooks() {
        super.applyHooks();
    }

    @Override
    public void inject() throws Throwable {
        getHookObject().injectService("content");
    }
    @Override
    public boolean isEnvBad() {
        return ServiceManager.getService("content") != getHookObject();
    }

}
