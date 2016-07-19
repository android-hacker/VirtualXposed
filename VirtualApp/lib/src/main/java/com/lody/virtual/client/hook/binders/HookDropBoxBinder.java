package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

import com.android.internal.os.IDropBoxManagerService;
import com.lody.virtual.client.hook.base.HookBinder;

/**
 * @author Lody
 */

public class HookDropBoxBinder extends HookBinder<IDropBoxManagerService> {

    @Override
    protected IBinder queryBaseBinder() {
        return ServiceManager.getService(Context.DROPBOX_SERVICE);
    }

    @Override
    protected IDropBoxManagerService createInterface(IBinder baseBinder) {
        return IDropBoxManagerService.Stub.asInterface(baseBinder);
    }
}