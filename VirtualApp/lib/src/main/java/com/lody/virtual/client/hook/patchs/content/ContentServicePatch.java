package com.lody.virtual.client.hook.patchs.content;


import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.ContentBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */

public class ContentServicePatch extends PatchDelegate<ContentBinderDelegate> {
    @Override
    protected ContentBinderDelegate createHookDelegate() {
        return new ContentBinderDelegate();
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
    }

    @Override
    public void inject() throws Throwable {
        getHookDelegate().replaceService("content");
    }
    @Override
    public boolean isEnvBad() {
        return ServiceManager.getService.call("content") != getHookDelegate();
    }

}
