package com.lody.virtual.client.hook.patchs.media.session;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.SessionBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@Patch({CreateSession.class})
public class SessionManagerPatch extends PatchDelegate<SessionBinderDelegate> {
	@Override
	protected SessionBinderDelegate createHookDelegate() {
		return new SessionBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.MEDIA_SESSION_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call(Context.MEDIA_SESSION_SERVICE);
	}
}
