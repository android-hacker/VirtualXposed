package com.lody.virtual.client.hook.patchs.content;

import android.content.IContentService;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.StaticHook;
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
        addHook(new StaticHook("registerContentObserver")).replaceLastUserId();
        addHook(new StaticHook("notifyChange")).replaceLastUserId();
        addHook(new StaticHook("syncAsUser")).replaceLastUserId();
        addHook(new StaticHook("getSyncAutomaticallyAsUser")).replaceLastUserId();
        addHook(new StaticHook("setSyncAutomaticallyAsUser")).replaceLastUserId();
        addHook(new StaticHook("getIsSyncableAsUser")).replaceLastUserId();
        addHook(new StaticHook("setMasterSyncAutomaticallyAsUser")).replaceLastUserId();
        addHook(new StaticHook("getMasterSyncAutomaticallyAsUser")).replaceLastUserId();
        addHook(new StaticHook("getCurrentSyncsAsUser")).replaceLastUserId();
        addHook(new StaticHook("getSyncAdapterTypesAsUser")).replaceLastUserId();
        addHook(new StaticHook("getSyncAdapterPackagesForAuthorityAsUser")).replaceLastUserId();
        addHook(new StaticHook("getSyncStatusAsUser")).replaceLastUserId();
        addHook(new StaticHook("isSyncPendingAsUser")).replaceLastUserId();
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
