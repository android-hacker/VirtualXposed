package com.lody.virtual.client.hook.patchs.am;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.os.Build;
import android.os.IBinder;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;


/**
 * @author prife
 * @see android.app.IActivityManager#setTaskDescription(IBinder token,
 * 				ActivityManager.TaskDescription values)
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
/* package */ class SetTaskDescription extends Hook {
	static final String VACLIENT_SUFFIX = "[VA]";
	@Override
	public String getName() {
		return "setTaskDescription";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ActivityManager.TaskDescription td = (ActivityManager.TaskDescription)args[1];

		String label = td.getLabel();
		if (label == null) {
			Application app = VClientImpl.getClient().getCurrentApplication();
			label = app.getApplicationInfo().loadLabel(app.getPackageManager()) + VACLIENT_SUFFIX;
			td.setLabel(label);
		} else if (label != null && !label.endsWith(VACLIENT_SUFFIX)) {
			td.setLabel(label + VACLIENT_SUFFIX);
		}

		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}