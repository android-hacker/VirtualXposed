package com.lody.virtual.client.hook.patchs.job;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.binders.JobBinderDelegate;

import mirror.android.os.ServiceManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@Patch({Schedule.class,})
public class JobPatch extends PatchDelegate<JobBinderDelegate> {
	@Override
	protected JobBinderDelegate createHookDelegate() {
		return new JobBinderDelegate();
	}

	@Override
	public void inject() throws Throwable {
		getHookDelegate().replaceService(Context.JOB_SCHEDULER_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return getHookDelegate() != ServiceManager.getService.call(Context.JOB_SCHEDULER_SERVICE);
	}
}
