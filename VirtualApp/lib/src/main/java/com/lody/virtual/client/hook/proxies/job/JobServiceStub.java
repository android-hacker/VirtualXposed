package com.lody.virtual.client.hook.proxies.job;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.ipc.VJobScheduler;

import java.lang.reflect.Method;

import mirror.android.app.job.IJobScheduler;

/**
 * @author Lody
 *
 * @see android.app.job.JobScheduler
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobServiceStub extends BinderInvocationProxy {

	public JobServiceStub() {
		super(IJobScheduler.Stub.asInterface, Context.JOB_SCHEDULER_SERVICE);
	}

	@Override
	protected void onBindMethods() {
		super.onBindMethods();
		addMethodProxy(new schedule());
		addMethodProxy(new getAllPendingJobs());
		addMethodProxy(new cancelAll());
		addMethodProxy(new cancel());
	}


	private class schedule extends MethodProxy {

		@Override
		public String getMethodName() {
			return "schedule";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			JobInfo jobInfo = (JobInfo) args[0];
			return VJobScheduler.get().schedule(jobInfo);
		}
	}

	private class getAllPendingJobs extends MethodProxy {

		@Override
		public String getMethodName() {
			return "getAllPendingJobs";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			return VJobScheduler.get().getAllPendingJobs();
		}
	}

	private class cancelAll extends MethodProxy {

		@Override
		public String getMethodName() {
			return "cancelAll";
		}

		@Override
		public Object call(Object who, Method method, Object... args) throws Throwable {
			VJobScheduler.get().cancelAll();
			return 0;
		}
	}

	private class cancel extends MethodProxy {

		@Override
		public String getMethodName() {
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
