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
/* package */ class Hook_QueryIntentReceivers extends Hook {

	@Override
	public String getName() {
		return "queryIntentReceivers";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		int userId = isAppProcess() ? VUserHandle.myUserId() : VUserHandle.USER_OWNER;
		if (args.length > 3 && args[3] instanceof Integer) {
			userId = (int) args[3];
		}
		return VPackageManager.getInstance().queryIntentReceivers((Intent) args[0], (String) args[1],
				(Integer) args[2], userId);
	}
}
