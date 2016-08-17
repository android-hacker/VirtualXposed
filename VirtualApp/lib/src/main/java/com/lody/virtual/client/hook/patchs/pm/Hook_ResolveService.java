package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 *         原型: public ResolveInfo resolveService(Intent intent, String
 *         resolvedType, int flags, int userId)
 */
/* package */ class Hook_ResolveService extends Hook {

	@Override
	public String getName() {
		return "resolveService";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Intent intent = (Intent) args[0];
		String resolvedType = (String) args[1];
		int flags = (int) args[2];
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 3 && args[3] instanceof Integer) {
			userId = (int) args[3];
		}
		ResolveInfo resolveInfo = VPackageManager.getInstance().resolveService(intent, resolvedType, flags, userId);
		if (resolveInfo == null) {
			resolveInfo = (ResolveInfo) method.invoke(who, args);
		}
		return resolveInfo;
	}
}
