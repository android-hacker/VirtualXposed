package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import android.accounts.Account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#setUserData(Account, String, String)
 *
 */

public class Hook_SetUserData extends Hook {

	@Override
	public String getName() {
		return "setUserData";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Account account = (Account) args[0];
		String key = (String) args[1];
		String value = (String) args[2];
		LocalAccountManager.getInstance().setUserData(account, key, value);
		return 0;
	}
}
