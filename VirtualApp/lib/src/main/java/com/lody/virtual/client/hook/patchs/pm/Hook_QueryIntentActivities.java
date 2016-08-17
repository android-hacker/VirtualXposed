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
/* package */ class Hook_QueryIntentActivities extends Hook {

	@Override
	public String getName() {
		return "queryIntentActivities";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {

		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 3 && args[2] instanceof Integer) {
			userId = (int) args[3];
		}
		List<ResolveInfo> result = (List<ResolveInfo>) method.invoke(who, args);
		List<ResolveInfo> pluginResult = VPackageManager.getInstance().queryIntentActivities((Intent) args[0],
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
