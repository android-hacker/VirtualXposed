package com.lody.virtual.client.hook.patchs.pm;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;

import android.content.Intent;

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
		return VPackageManager.getInstance().queryIntentReceivers((Intent) args[0], (String) args[1],
				(Integer) args[2]);
	}
}
