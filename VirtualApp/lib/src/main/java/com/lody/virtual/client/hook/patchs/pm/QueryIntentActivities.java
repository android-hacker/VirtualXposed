package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Lody
 *
 *
 * @see android.content.pm.IPackageManager
 *
 *      原型： public List<ResolveInfo> queryIntentActivities(Intent intent, String
 *      resolvedType, int flags, int userId)
 */
@SuppressWarnings("unchecked")
/* package */ class QueryIntentActivities extends Hook {

	@Override
	public String getName() {
		return "queryIntentActivities";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {

		int userId = VUserHandle.myUserId();
		List<ResolveInfo> result = (List<ResolveInfo>) method.invoke(who, args);
		List<ResolveInfo> pluginResult = VPackageManager.get().queryIntentActivities((Intent) args[0],
				(String) args[1], (Integer) args[2], userId);
		if (pluginResult != null && !pluginResult.isEmpty()) {
			result.addAll(pluginResult);
		}
		return result;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
