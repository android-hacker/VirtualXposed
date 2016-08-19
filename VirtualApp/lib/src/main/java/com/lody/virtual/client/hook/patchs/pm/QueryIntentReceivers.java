package com.lody.virtual.client.hook.patchs.pm;

import android.content.Intent;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class QueryIntentReceivers extends Hook {

	@Override
	public String getName() {
		return "queryIntentReceivers";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int userId = VUserHandle.myUserId();
		return VPackageManager.get().queryIntentReceivers((Intent) args[0], (String) args[1],
				(Integer) args[2], userId);
	}
}
