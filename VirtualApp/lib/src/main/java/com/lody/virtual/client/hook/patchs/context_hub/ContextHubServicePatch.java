package com.lody.virtual.client.hook.patchs.context_hub;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ResultStaticHook;

import mirror.android.hardware.location.IContextHubService;

public class ContextHubServicePatch extends PatchBinderDelegate {

    public ContextHubServicePatch() {
        super(IContextHubService.Stub.asInterface, "contexthub_service");
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new ResultStaticHook("registerCallback", 0));
    }
}