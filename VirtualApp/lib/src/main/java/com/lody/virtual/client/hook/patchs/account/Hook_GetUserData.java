package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import android.accounts.Account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getUserData(Account, String)
 *
 */

public class Hook_GetUserData extends Hook {

	@Override
	public String getName() {
		return "getUserData";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Account account = (Account) args[0];
		String key = (String) args[1];
		return LocalAccountManager.getInstance().getUserData(account, key);
	}
}
