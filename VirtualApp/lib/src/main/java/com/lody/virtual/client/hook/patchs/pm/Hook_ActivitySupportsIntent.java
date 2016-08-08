package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;

import android.content.ComponentName;
import android.content.Intent;

/**
 * @author Lody
 */

public class Hook_ActivitySupportsIntent extends Hook {
	@Override
	public String getName() {
		return "activitySupportsIntent";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		ComponentName component = (ComponentName) args[0];
		Intent intent = (Intent) args[1];
		String resolvedType = (String) args[2];
		return VPackageManager.getInstance().activitySupportsIntent(component, intent, resolvedType);
	}
}
