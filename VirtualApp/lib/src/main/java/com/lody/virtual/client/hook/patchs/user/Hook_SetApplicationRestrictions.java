package com.lody.virtual.client.hook.patchs.user;

import android.os.Bundle;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see android.os.IUserManager#setApplicationRestrictions(String, Bundle, int)
 */
/* package */ class Hook_SetApplicationRestrictions extends Hook {

	@Override
	public String getName() {
		return "setApplicationRestrictions";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
