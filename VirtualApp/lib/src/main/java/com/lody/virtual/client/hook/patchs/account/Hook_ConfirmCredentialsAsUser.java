package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VAccountManager;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#confirmCredentialsAsUser(IAccountManagerResponse,
 *      Account, Bundle, boolean, int)
 *
 */

public class Hook_ConfirmCredentialsAsUser extends Hook {

	@Override
	public String getName() {
		return "confirmCredentialsAsUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IAccountManagerResponse response = (IAccountManagerResponse) args[0];
		Account account = (Account) args[1];
		Bundle options = (Bundle) args[2];
		boolean expectActivityLaunch = (boolean) args[3];
		VAccountManager.getInstance().confirmCredentials(response, account, options, expectActivityLaunch);
		return 0;
	}
}
