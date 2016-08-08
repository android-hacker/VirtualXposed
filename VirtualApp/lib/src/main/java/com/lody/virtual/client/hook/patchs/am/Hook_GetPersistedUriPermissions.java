package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 *
 * @see android.app.IActivityManager#getPersistedUriPermissions(String, boolean)
 */
/* package */ class Hook_GetPersistedUriPermissions extends Hook {

	@Override
	public String getName() {
		return "getPersistedUriPermissions";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
