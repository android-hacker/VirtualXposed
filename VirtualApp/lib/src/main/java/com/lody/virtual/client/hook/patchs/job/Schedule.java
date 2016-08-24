package com.lody.virtual.client.hook.patchs.job;

import static android.app.job.JobScheduler.RESULT_FAILURE;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.os.Build;

/**
 * @author Lody
 * @see android.app.job.IJobScheduler#schedule(JobInfo)
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class Schedule extends Hook {

	@Override
	public String getName() {
		return "schedule";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return RESULT_FAILURE;
	}

}
