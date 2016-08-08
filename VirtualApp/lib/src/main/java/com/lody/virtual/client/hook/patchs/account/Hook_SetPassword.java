package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VAccountManager;

import android.accounts.Account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#setPassword(Account, String)
 *
 */

public class Hook_SetPassword extends Hook {

	@Override
	public String getName() {
		return "setPassword";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Account account = (Account) args[0];
		String password = (String) args[1];
		VAccountManager.getInstance().setPassword(account, password);
		return 0;
	}
}
