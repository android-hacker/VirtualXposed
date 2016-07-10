package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.IMediaRouterService;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class HookMediaRouterBinder extends HookBinder<IMediaRouterService> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.MEDIA_ROUTER_SERVICE);
	}

	@Override
	protected IMediaRouterService createInterface(IBinder baseBinder) {
		return IMediaRouterService.Stub.asInterface(baseBinder);
	}
}
