package com.lody.virtual.client.hook.patchs.media.session;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;

import mirror.android.media.session.ISessionManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@Patch({CreateSession.class})
public class SessionManagerPatch extends PatchBinderDelegate {

	public SessionManagerPatch() {
		super(ISessionManager.Stub.TYPE, Context.MEDIA_SESSION_SERVICE);
	}
}
