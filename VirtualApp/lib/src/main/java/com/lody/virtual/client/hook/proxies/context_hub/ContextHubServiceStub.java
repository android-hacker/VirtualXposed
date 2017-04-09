package com.lody.virtual.client.hook.proxies.context_hub;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ResultStaticMethodProxy;

import mirror.android.hardware.location.IContextHubService;

public class ContextHubServiceStub extends BinderInvocationProxy {

    public ContextHubServiceStub() {
        super(IContextHubService.Stub.asInterface, "contexthub_service");
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addMethodProxy(new ResultStaticMethodProxy("registerCallback", 0));
    }
}