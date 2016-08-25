package com.lody.virtual.client.hook.binders;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;

import com.lody.virtual.client.hook.base.HookBinderDelegate;

import mirror.android.app.IJobScheduler;
import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobBinderDelegate extends HookBinderDelegate {


	@Override
	protected IInterface createInterface() {
		IBinder binder = ServiceManager.getService.call(Context.JOB_SCHEDULER_SERVICE);
		return IJobScheduler.Stub.asInterface.call(binder);
	}
}
