package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VAccountManager;

import android.accounts.Account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#setAuthToken(Account, String, String)
 *
 */

public class Hook_SetAuthToken extends Hook {

	@Override
	public String getName() {
		return "setAuthToken";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Account account = (Account) args[0];
		String authTokenType = (String) args[1];
		String authToken = (String) args[2];
		VAccountManager.getInstance().setAuthToken(account, authTokenType, authToken);
		return 0;
	}
}
