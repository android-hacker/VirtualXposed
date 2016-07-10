package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import android.os.Build;

/**
 * @author Lody
 */
/* package */ class Hook_GetAccounts extends Hook<AccountManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetAccounts(AccountManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getAccounts";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (Build.VERSION.SDK_INT >= 19) {
			HookUtils.replaceLastAppPkg(args);
		}
		return method.invoke(who, args);
	}
}
