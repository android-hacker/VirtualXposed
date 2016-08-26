package com.lody.virtual.client.hook.patchs.isub;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;
import com.lody.virtual.client.hook.base.ReplaceLastPkgHook;
import com.lody.virtual.client.hook.binders.ISubBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */

public class ISubPatch extends PatchDelegate<ISubBinderDelegate> {
    @Override
    protected ISubBinderDelegate createHookDelegate() {
        return new ISubBinderDelegate();
    }

    @Override
    protected void afterHookApply(ISubBinderDelegate delegate) {
        super.afterHookApply(delegate);
        addHook(new ReplaceCallingPkgHook("getAllSubInfoList"));
        addHook(new ReplaceCallingPkgHook("getAllSubInfoCount"));
        addHook(new ReplaceLastPkgHook("getActiveSubscriptionInfo"));
        addHook(new ReplaceLastPkgHook("getActiveSubscriptionInfoForIccId"));
        addHook(new ReplaceLastPkgHook("getActiveSubscriptionInfoForSimSlotIndex"));
        addHook(new ReplaceLastPkgHook("getActiveSubscriptionInfoList"));
        addHook(new ReplaceLastPkgHook("getActiveSubInfoCount"));
        addHook(new ReplaceLastPkgHook("getSubscriptionProperty"));
    }

    @Override
    public void inject() throws Throwable {
        getHookDelegate().replaceService("isub");
    }

    @Override
    public boolean isEnvBad() {
        return ServiceManager.getService.call("isub") != getHookDelegate();
    }
}
