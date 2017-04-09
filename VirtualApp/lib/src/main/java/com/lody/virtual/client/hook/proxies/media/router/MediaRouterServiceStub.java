package com.lody.virtual.client.hook.proxies.media.router;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.android.media.IMediaRouterService;

/**
 * @author Lody
 * @see android.media.MediaRouter
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MediaRouterServiceStub extends BinderInvocationProxy {

    public MediaRouterServiceStub() {
        super(IMediaRouterService.Stub.asInterface, Context.MEDIA_ROUTER_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("registerClientAsUser"));
    }
}
