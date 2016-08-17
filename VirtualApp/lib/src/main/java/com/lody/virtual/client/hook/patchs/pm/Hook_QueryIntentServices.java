package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lody
 *
 * @see android.content.pm.IPackageManager#queryIntentServices(Intent, String, int, int)
 *
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
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 3 && args[3] instanceof Integer) {
			userId = (int) args[3];
		}
		List<ResolveInfo> result = (List<ResolveInfo>) method.invoke(who, args);
		List<ResolveInfo> appResult = VPackageManager.getInstance().queryIntentServices((Intent) args[0],
				(String) args[1], (Integer) args[2], userId);

		if (result == null) {
			result = new ArrayList<ResolveInfo>();
		}
		if (!result.isEmpty()) {
			return result;
		}

		if (appResult != null && !appResult.isEmpty()) {
			return appResult;
		}
		return result;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
