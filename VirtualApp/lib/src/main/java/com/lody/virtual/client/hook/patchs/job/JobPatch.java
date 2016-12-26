package com.lody.virtual.client.hook.patchs.job;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.ipc.VJobScheduler;

import java.lang.reflect.Method;

import mirror.android.app.job.IJobScheduler;

/**
 * @author Lody
 *
 * @see android.app.job.JobScheduler
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobPatch extends PatchBinderDelegate {

	public JobPatch() {
		super(IJobScheduler.Stub.TYPE, Context.JOB_SCHEDULER_SERVICE);
	}

	@Override
	protected void onBindHooks() {
		super.onBindHooks();
		addHook(new schedule());
		addHook(new getAllPendingJobs());
		addHook(new cancelAll());
		addHook(new cancel());
	}


	private class schedule extends Hook {

		@Override
		public String getName() {
			return "schedule";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			JobInfo jobInfo = (JobInfo) args[0];
			return VJobScheduler.get().schedule(jobInfo);
		}
	}

	private class getAllPendingJobs extends Hook {

		@Override
		public String getName() {
			return "getAllPendingJobs";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			return VJobScheduler.get().getAllPendingJobs();
		}
	}

	private class cancelAll extends Hook {

		@Override
		public String getName() {
			return "cancelAll";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			VJobScheduler.get().cancelAll();
			return 0;
		}
	}

	private class cancel extends Hook {

		@Override
		public String getName() {
			return "cancel";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			int jobId = (int) args[0];
			VJobScheduler.get().cancel(jobId);
			return 0;
		}
	}
}
