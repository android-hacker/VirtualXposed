package com.lody.virtual.client.hook.patchs.account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class Hook_GetAccountsAsUser extends Hook<AccountManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetAccountsAsUser(AccountManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getAccountsAsUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		return method.invoke(who, args);
	}
}
