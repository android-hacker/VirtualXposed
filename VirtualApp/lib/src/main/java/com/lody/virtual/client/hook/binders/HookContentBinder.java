package com.lody.virtual.client.hook.binders;

import android.content.IContentService;
import android.os.IBinder;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.HookBinder;

/**
 * @author Lody
 */

public class HookContentBinder extends HookBinder<IContentService> {
    @Override
    protected IBinder queryBaseBinder() {
        return ServiceManager.getService("content");
    }

    @Override
    protected IContentService createInterface(IBinder baseBinder) {
        return IContentService.Stub.asInterface(baseBinder);
    }
}
