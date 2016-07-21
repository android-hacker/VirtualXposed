package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalPackageManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_QueryIntentReceivers extends Hook {

	@Override
	public String getName() {
		return "queryIntentReceivers";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return LocalPackageManager.getInstance().queryIntentReceivers((Intent) args[0], (String) args[1],
				(Integer) args[2]);
	}
}
