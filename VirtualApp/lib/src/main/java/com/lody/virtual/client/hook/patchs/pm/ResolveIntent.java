package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;
import android.content.pm.ResolveInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 *         原型: resolveIntent(Intent intent, String resolvedType, int flags, int
 *         userId)
 */
/* package */ class ResolveIntent extends Hook {

	@Override
	public String getName() {
		return "resolveIntent";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		Intent intent = (Intent) args[0];
		String resolvedType = (String) args[1];
		int flags = (int) args[2];
		int userId = VUserHandle.myUserId();
		ResolveInfo resolveInfo = VPackageManager.get().resolveIntent(intent, resolvedType, flags, userId);
		if (resolveInfo == null) {
			resolveInfo = (ResolveInfo) method.invoke(who, args);
		}
		return resolveInfo;
	}
}
