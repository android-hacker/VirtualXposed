package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import android.accounts.Account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#peekAuthToken(Account, String)
 *
 */

public class Hook_PeekAuthToken extends Hook {

	@Override
	public String getName() {
		return "peekAuthToken";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Account account = (Account) args[0];
		final String authTokenType = (String) args[1];
		return LocalAccountManager.getInstance().peekAuthToken(account, authTokenType);
	}
}
