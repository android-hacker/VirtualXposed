package com.lody.virtual.client.hook.patchs.media.router;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.MediaRouterBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 * @see android.media.MediaRouter
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
@Patch({RegisterClientAsUser.class})
public class MediaRouterServicePatch extends PatchDelegate<MediaRouterBinderDelegate> {
	@Override
	protected MediaRouterBinderDelegate createHookDelegate() {
		return new MediaRouterBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.MEDIA_ROUTER_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService.call(Context.MEDIA_ROUTER_SERVICE) != getHookDelegate();
	}
}
