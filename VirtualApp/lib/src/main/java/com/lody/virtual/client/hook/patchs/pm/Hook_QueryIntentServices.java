package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 *
 */
@SuppressWarnings("unchecked")
/* package */ class Hook_QueryIntentServices extends Hook {

	@Override
	public String getName() {
		return "queryIntentServices";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		if (isServiceProcess()) {
			return LocalPackageManager.getInstance().queryIntentServices((Intent) args[0],
					(String) args[1], (Integer) args[2]);
		}

		List<ResolveInfo> result = (List<ResolveInfo>) method.invoke(who, args);
		List<ResolveInfo> pluginResult = LocalPackageManager.getInstance().queryIntentServices((Intent) args[0],
				(String) args[1], (Integer) args[2]);

		if (result == null) {
			result = new ArrayList<ResolveInfo>();
		}
		if (!result.isEmpty()) {
			return result;
		}

		if (pluginResult != null && !pluginResult.isEmpty()) {
			return pluginResult;
		}
		return result;
	}
}
