package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import android.accounts.IAccountManagerResponse;
import android.os.Bundle;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#addAccountAsUser(IAccountManagerResponse,
 *      String, String, String[], boolean, Bundle, int)
 *
 */

public class Hook_AddAccountAsUser extends Hook {

	@Override
	public String getName() {
		return "addAccountAsUser";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IAccountManagerResponse response = (IAccountManagerResponse) args[0];
		String accountType = (String) args[1];
		String authTokenType = (String) args[2];
		String[] requiredFeatures = (String[]) args[3];
		boolean expectActivityLaunch = (boolean) args[4];
		Bundle options = (Bundle) args[5];
		LocalAccountManager.getInstance().addAcount(response, accountType, authTokenType, requiredFeatures,
				expectActivityLaunch, options);
		return 0;
	}
}
