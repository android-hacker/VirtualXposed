package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;
import com.lody.virtual.client.service.ServiceManagerNative;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.IRestrictionsManager;
import android.os.Build;
import android.os.IBinder;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HookRestrictionBinder extends HookBinder<IRestrictionsManager> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManagerNative.getService(Context.RESTRICTIONS_SERVICE);
	}

	@Override
	protected IRestrictionsManager createInterface(IBinder baseBinder) {
		return IRestrictionsManager.Stub.asInterface(baseBinder);
	}
}
