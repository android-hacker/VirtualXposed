package com.lody.virtual.client.hook.patchs.user;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.os.IUserManager#getApplicationRestrictionsForUser(String, int)
 */
/* package */ class Hook_GetApplicationRestrictionsForUser extends Hook {

	@Override
	public String getName() {
		return "getApplicationRestrictionsForUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
