package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;
import com.lody.virtual.client.ipc.VActivityManager;

import android.content.Intent;

/**
 * @author Lody
 *
 */
/* package */ class PeekService extends Hook {

	@Override
	public String getName() {
		return "peekService";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		Intent service = (Intent) args[0];
		String resolvedType = (String) args[1];
		return VActivityManager.get().peekService(service, resolvedType);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
