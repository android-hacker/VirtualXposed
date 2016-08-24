package com.lody.virtual.client.hook.patchs.media.router;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookMediaRouterBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.IMediaRouterService;
import android.os.Build;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 *
 *         在 API 16 加入
 * @see IMediaRouterService
 * @see android.media.MediaRouter
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@Patch({RegisterClientAsUser.class})
public class MediaRouterServicePatch extends PatchObject<IMediaRouterService, HookMediaRouterBinder> {
	@Override
	protected HookMediaRouterBinder initHookObject() {
		return new HookMediaRouterBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.MEDIA_ROUTER_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.MEDIA_ROUTER_SERVICE) != getHookObject();
	}
}
