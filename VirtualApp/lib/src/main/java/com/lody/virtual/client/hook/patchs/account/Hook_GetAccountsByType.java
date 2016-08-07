package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VAccountManager;

/**
 * @author Lody
 *
 *         Account[] getAccountsByType(String type);
 */

public class Hook_GetAccountsByType extends Hook {

	@Override
	public String getName() {
		return "getAccountsByType";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String type = (String) args[0];
		return VAccountManager.getInstance().getAccounts(type);
	}
}
