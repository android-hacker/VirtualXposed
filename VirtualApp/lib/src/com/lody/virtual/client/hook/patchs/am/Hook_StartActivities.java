package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.ArrayIndex;

import android.app.IApplicationThread;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#startActivities(IApplicationThread, String,
 *      Intent[], String[], IBinder, Bundle, int)
 */
/* package */ class Hook_StartActivities extends Hook<ActivityManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_StartActivities(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "startActivities";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int intentArrayIndex = ArrayIndex.indexOfFirst(args, Intent[].class);
		Intent[] intents = (Intent[]) args[intentArrayIndex];
		for (int N = 0; N < intents.length; N++) {
			ActivityUtils.replaceIntent(null, intents, N);
		}
		return method.invoke(who, args);
	}
}
