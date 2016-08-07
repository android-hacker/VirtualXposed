package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;

/**
 * @author Lody
 *
 *
 *         Android 4.4+
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
		return VPackageManager.getInstance().queryIntentContentProviders((Intent) args[0], (String) args[1],
				(Integer) args[2]);
	}
}
