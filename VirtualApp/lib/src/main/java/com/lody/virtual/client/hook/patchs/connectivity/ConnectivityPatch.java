package com.lody.virtual.client.hook.patchs.connectivity;

import android.content.Context;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;

import mirror.android.net.IConnectivityManager;

/**
 * @author legency
 */
public class ConnectivityPatch extends PatchBinderDelegate {

    public ConnectivityPatch() {
        super(IConnectivityManager.Stub.TYPE, Context.CONNECTIVITY_SERVICE);
    }
}
