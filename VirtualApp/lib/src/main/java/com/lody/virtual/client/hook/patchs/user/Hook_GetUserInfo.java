package com.lody.virtual.client.hook.patchs.user;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class Hook_GetUserInfo extends Hook<UserManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetUserInfo(UserManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getUserInfo";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return null;
	}
}
