package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;

import android.accounts.Account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#addSharedAccountAsUser(Account, int)
 *
 */

public class Hook_AddSharedAccountAsUser extends Hook {

	@Override
	public String getName() {
		return "addSharedAccountAsUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return false;
	}
}
