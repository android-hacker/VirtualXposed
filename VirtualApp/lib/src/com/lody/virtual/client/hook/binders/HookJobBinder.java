package com.lody.virtual.client.hook.binders;

import com.lody.virtual.client.hook.base.HookBinder;

import android.annotation.TargetApi;
import android.app.job.IJobScheduler;
import android.content.Context;
import android.os.Build;
import android.os.IBinder;
import android.os.ServiceManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class HookJobBinder extends HookBinder<IJobScheduler> {

	@Override
	protected IBinder queryBaseBinder() {
		return ServiceManager.getService(Context.JOB_SCHEDULER_SERVICE);
	}

	@Override
	protected IJobScheduler createInterface(IBinder baseBinder) {
		return IJobScheduler.Stub.asInterface(baseBinder);
	}
}
