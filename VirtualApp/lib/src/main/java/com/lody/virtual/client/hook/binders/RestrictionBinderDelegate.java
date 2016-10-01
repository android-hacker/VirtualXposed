package com.lody.virtual.client.hook.binders;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.content.IRestrictionsManager;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RestrictionBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.RESTRICTIONS_SERVICE);
		return IRestrictionsManager.Stub.asInterface.call(binder);
	}
}
