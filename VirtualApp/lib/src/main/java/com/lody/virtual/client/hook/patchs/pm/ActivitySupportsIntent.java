package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;

import android.content.ComponentName;
import android.content.Intent;

/**
 * @author Lody
 */

public class ActivitySupportsIntent extends Hook {
	@Override
	public String getName() {
		return "activitySupportsIntent";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		ComponentName component = (ComponentName) args[0];
		Intent intent = (Intent) args[1];
		String resolvedType = (String) args[2];
		return VPackageManager.get().activitySupportsIntent(component, intent, resolvedType);
	}
}
