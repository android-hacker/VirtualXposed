package com.lody.virtual.client.hook.patchs.media.session;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.session.ISessionManager;
import android.os.Build;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.base.StaticHook;
import com.lody.virtual.client.hook.binders.HookSessionBinder;

/**
 * @author Lody
 *
 *
 *         在API 21 加入
 * @see ISessionManager
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@Patch({Hook_CreateSession.class})
public class SessionManagerPatch extends PatchObject<ISessionManager, HookSessionBinder> {
	@Override
	protected HookSessionBinder initHookObject() {
		return new HookSessionBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.MEDIA_SESSION_SERVICE);
	}

	@Override
	protected void applyHooks() {
		super.applyHooks();
		addHook(new StaticHook("getSessions")).replaceLastUserId();
		addHook(new StaticHook("addSessionsListener")).replaceLastUserId();
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.MEDIA_SESSION_SERVICE);
	}
}
