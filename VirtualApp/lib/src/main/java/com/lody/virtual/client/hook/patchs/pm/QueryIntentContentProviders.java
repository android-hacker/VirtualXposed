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
/* package */ class QueryIntentContentProviders extends Hook {

	@Override
	public String getName() {
		return "queryIntentContentProviders";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		int userId = VUserHandle.myUserId();
		return VPackageManager.get().queryIntentContentProviders((Intent) args[0], (String) args[1],
				(Integer) args[2], userId);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
