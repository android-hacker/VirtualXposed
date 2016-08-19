package com.lody.virtual.client.hook.patchs.job;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookJobBinder;

import android.annotation.TargetApi;
import android.app.job.IJobScheduler;
import android.content.Context;
import android.os.Build;
import android.os.ServiceManager;

/**
 * @author Lody
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
@Patch({Schedule.class,})
public class JobPatch extends PatchObject<IJobScheduler, HookJobBinder> {
	@Override
	protected HookJobBinder initHookObject() {
		return new HookJobBinder();
	}

	@Override
	public void inject() throws Throwable {
		getHookObject().injectService(Context.JOB_SCHEDULER_SERVICE);
	}

	@Override
	public boolean isEnvBad() {
		return getHookObject() != ServiceManager.getService(Context.JOB_SCHEDULER_SERVICE);
	}
}
