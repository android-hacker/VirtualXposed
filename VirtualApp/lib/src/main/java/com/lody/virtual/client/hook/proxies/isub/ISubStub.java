package com.lody.virtual.client.hook.proxies.isub;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;

import mirror.com.android.internal.telephony.ISub;

/**
 * @author Lody
 */
public class ISubStub extends BinderInvocationProxy {

    public ISubStub() {
        super(ISub.Stub.asInterface, "isub");
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getAllSubInfoList"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getAllSubInfoCount"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getActiveSubscriptionInfo"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getActiveSubscriptionInfoForIccId"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getActiveSubscriptionInfoForSimSlotIndex"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getActiveSubscriptionInfoList"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getActiveSubInfoCount"));
        addMethodProxy(new ReplaceLastPkgMethodProxy("getSubscriptionProperty"));
    }
}
