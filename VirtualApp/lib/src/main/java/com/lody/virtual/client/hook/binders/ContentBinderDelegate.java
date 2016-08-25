package com.lody.virtual.client.hook.binders;

import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.content.IContentService;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */

public class ContentBinderDelegate extends HookBinderDelegate {

    @Override
    protected IInterface createInterface() {
        IBinder binder = ServiceManager.getService.call("content");
        return IContentService.Stub.asInterface.call(binder);
    }
}
