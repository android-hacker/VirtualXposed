package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.accounts.Account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#removeSharedAccountAsUser(Account, int)
 *
 */

public class Hook_RemoveSharedAccountAsUser extends Hook {

	@Override
	public String getName() {
		return "removeSharedAccountAsUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return false;
	}
}
