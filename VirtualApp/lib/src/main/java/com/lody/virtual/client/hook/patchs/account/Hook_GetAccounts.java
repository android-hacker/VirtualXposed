package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VAccountManager;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAccounts(String, String)
 *
 */

public class Hook_GetAccounts extends Hook {

	@Override
	public String getName() {
		return "getAccounts";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String accountType = (String) args[0];
		return VAccountManager.getInstance().getAccounts(accountType);
	}
}
