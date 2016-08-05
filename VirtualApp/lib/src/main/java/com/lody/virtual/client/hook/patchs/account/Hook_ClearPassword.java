package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import android.accounts.Account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#clearPassword(Account)
 *
 */

public class Hook_ClearPassword extends Hook {

	@Override
	public String getName() {
		return "clearPassword";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Account account = (Account) args[0];
		LocalAccountManager.getInstance().clearPassword(account);
		return 0;
	}
}
