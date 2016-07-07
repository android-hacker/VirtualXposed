package com.lody.virtual.client.hook.patchs.job;

import static android.app.job.JobScheduler.RESULT_FAILURE;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.app.job.JobInfo;

/**
 * @author Lody
 * @see android.app.job.IJobScheduler#schedule(JobInfo)
 */

/* package */ class Hook_Schedule extends Hook<JobPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_Schedule(JobPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "schedule";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (true) {
			return RESULT_FAILURE;
		}
		return method.invoke(who, args);
	}

}
