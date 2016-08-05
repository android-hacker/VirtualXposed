package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#updateCredentials(IAccountManagerResponse,
 *      Account, String, boolean, Bundle)
 *
 */

public class Hook_UpdateCredentials extends Hook {

	@Override
	public String getName() {
		return "updateCredentials";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IAccountManagerResponse response = (IAccountManagerResponse) args[0];
		Account account = (Account) args[1];
		String authTokenType = (String) args[2];
		boolean expectActivityLaunch = (boolean) args[3];
		Bundle options = (Bundle) args[4];
		LocalAccountManager.getInstance().updateCredentials(response, account, authTokenType, expectActivityLaunch,
				options);
		return 0;
	}
}
