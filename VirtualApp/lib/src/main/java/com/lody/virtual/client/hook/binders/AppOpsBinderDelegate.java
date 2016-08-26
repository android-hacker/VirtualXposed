package com.lody.virtual.client.hook.binders;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.os.ServiceManager;
import mirror.com.android.internal.app.IAppOpsService;

/**
 * @author Lody
 *
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class AppOpsBinderDelegate extends HookBinderDelegate {

	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.APP_OPS_SERVICE);
		return IAppOpsService.Stub.asInterface.call(binder);
	}
}
