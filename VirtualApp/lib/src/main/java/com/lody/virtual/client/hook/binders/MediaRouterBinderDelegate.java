package com.lody.virtual.client.hook.binders;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.media.IMediaRouterService;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaRouterBinderDelegate extends HookBinderDelegate {

    @Override
    public IInterface createInterface() {
        IBinder binder = ServiceManager.getService.call(Context.MEDIA_ROUTER_SERVICE);
        if (IMediaRouterService.Stub.asInterface != null) {
            return IMediaRouterService.Stub.asInterface.call(binder);
        }
        return null;
    }
}
