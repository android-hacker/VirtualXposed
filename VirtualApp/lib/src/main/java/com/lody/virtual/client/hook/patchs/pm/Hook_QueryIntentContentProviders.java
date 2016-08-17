package com.lody.virtual.client.hook.patchs.pm;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 *         Android 4.4+
 *
 *         @see android.content.pm.IPackageManager#queryIntentContentProviders(Intent, String, int, int)
 */
@SuppressWarnings("unchecked")
@TargetApi(Build.VERSION_CODES.KITKAT)
/* package */ class Hook_QueryIntentContentProviders extends Hook {

	@Override
	public String getName() {
		return "queryIntentContentProviders";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 3 && args[3] instanceof Integer) {
			userId = (int) args[3];
		}
		return VPackageManager.getInstance().queryIntentContentProviders((Intent) args[0], (String) args[1],
				(Integer) args[2], userId);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
