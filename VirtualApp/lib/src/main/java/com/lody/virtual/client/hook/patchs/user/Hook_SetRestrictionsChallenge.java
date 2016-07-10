package com.lody.virtual.client.hook.patchs.user;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 */
/* package */ class Hook_SetRestrictionsChallenge extends Hook<UserManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_SetRestrictionsChallenge(UserManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "setRestrictionsChallenge";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		return method.invoke(who, args);
	}
}
