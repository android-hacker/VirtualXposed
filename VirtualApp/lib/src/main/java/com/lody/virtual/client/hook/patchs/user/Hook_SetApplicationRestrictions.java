package com.lody.virtual.client.hook.patchs.user;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import android.os.Bundle;

/**
 * @author Lody
 *
 *
 * @see android.os.IUserManager#setApplicationRestrictions(String, Bundle, int)
 */
/* package */ class Hook_SetApplicationRestrictions extends Hook<UserManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_SetApplicationRestrictions(UserManagerPatch patchObject) {
		super(patchObject);
	}

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
