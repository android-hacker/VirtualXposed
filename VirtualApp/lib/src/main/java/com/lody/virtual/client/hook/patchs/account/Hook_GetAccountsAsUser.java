package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAccountsAsUser(String, int, String)
 */
/* package */ class Hook_GetAccountsAsUser extends Hook<AccountManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetAccountsAsUser(AccountManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getAccountsAsUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		String accountType = (String) args[0];
		if (!accountType.equals(AccountUtils.ACCOUNT_TYPE)) {
			args[0] = AccountUtils.ACCOUNT_TYPE;
		}
		Account[] accounts = (Account[]) method.invoke(who, args);
		for (Account account : accounts) {
			AccountUtils.restoreAccount(account);
		}
		return accounts;
	}
}
