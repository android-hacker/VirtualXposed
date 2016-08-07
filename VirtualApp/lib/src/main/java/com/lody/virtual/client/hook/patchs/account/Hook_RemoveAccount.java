package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VAccountManager;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#removeAccount(IAccountManagerResponse,
 *      Account, boolean)
 *
 */

public class Hook_RemoveAccount extends Hook {

	@Override
	public String getName() {
		return "removeAccount";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IAccountManagerResponse response = (IAccountManagerResponse) args[0];
		Account account = (Account) args[1];
		VAccountManager.getInstance().removeAccount(response, account);
		return 0;
	}
}
