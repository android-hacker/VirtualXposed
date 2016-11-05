package com.lody.virtual.client.hook.patchs.am;

import android.content.Intent;
import android.os.IBinder;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */

/* package */ class PublishService extends Hook {

	@Override
	public String getName() {
		return "publishService";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		IBinder token = (IBinder) args[0];
		if (!VActivityManager.get().isVAServiceToken(token)) {
			return method.invoke(who, args);
		}
		Intent intent = (Intent) args[1];
		IBinder service = (IBinder) args[2];
		VActivityManager.get().publishService(token, intent, service);
		return 0;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
