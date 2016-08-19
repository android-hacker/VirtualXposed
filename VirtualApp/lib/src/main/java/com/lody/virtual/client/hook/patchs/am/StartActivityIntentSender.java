package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.IBinder;

/**
 * @see android.app.ActivityManagerNative#startActivityIntentSender(IApplicationThread,
 *      IntentSender, Intent, String, IBinder, String, int, int, int, Bundle)
 * @see android.app.ActivityManagerNative#startActivity(IApplicationThread,
 *      String, Intent, String, IBinder, String, int, int, ProfilerInfo, Bundle)
 */
public class StartActivityIntentSender extends Hook {
	@Override
	public String getName() {
		return "startActivityIntentSender";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return super.onHook(who, method, args);
	}
}
