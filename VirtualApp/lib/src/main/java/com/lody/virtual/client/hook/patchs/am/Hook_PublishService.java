package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.os.IBinder;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalServiceManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#publishService(IBinder, Intent, IBinder)
 */

/* package */ class Hook_PublishService extends Hook {

	@Override
	public String getName() {
		return "publishService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IBinder token = (IBinder) args[0];
		Intent intent = (Intent) args[1];
		IBinder service = (IBinder) args[2];
		LocalServiceManager.getInstance().publishService(token, intent, service);
		return 0;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
