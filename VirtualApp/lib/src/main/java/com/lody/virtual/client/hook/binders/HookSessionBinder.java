package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.session.ISessionManager;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HookSessionBinder extends HookBinder<ISessionManager> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.MEDIA_SESSION_SERVICE);
	}

	@Override
	protected ISessionManager createInterface(IBinder baseBinder) {
		return ISessionManager.Stub.asInterface(baseBinder);
	}
}
