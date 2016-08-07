package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.os.IBinder;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#getCallingActivity(IBinder)
 *
 */

public class Hook_GetCallingActivity extends Hook {

	@Override
	public String getName() {
		return "getCallingActivity";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IBinder token = (IBinder) args[0];
		ActivityInfo activityInfo = VActivityManager.getInstance().getCallingActivity(token);
		if (activityInfo == null) {
			return null;
		}
		return new ComponentName(activityInfo.packageName, activityInfo.name);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
