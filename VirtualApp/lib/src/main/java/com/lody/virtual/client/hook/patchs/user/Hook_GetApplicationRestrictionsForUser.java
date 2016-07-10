package com.lody.virtual.client.hook.patchs.user;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 *
 * @see android.os.IUserManager#getApplicationRestrictionsForUser(String, int)
 */
/* package */ class Hook_GetApplicationRestrictionsForUser extends Hook<UserManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetApplicationRestrictionsForUser(UserManagerPatch patchObject) {
		super(patchObject);
	}

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
