package com.lody.virtual.client.hook.patchs.connectivity;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.ConnectivityBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author legency
 */
public class ConnectivityPatch extends PatchDelegate<ConnectivityBinderDelegate> {

    @Override
    protected ConnectivityBinderDelegate createHookDelegate() {
        return new ConnectivityBinderDelegate();
    }

    @Override
    public void inject() throws Throwable {
        getHookDelegate().replaceService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public boolean isEnvBad() {
        return ServiceManager.getService.call(Context.CONNECTIVITY_SERVICE) != getHookDelegate();
    }
}
