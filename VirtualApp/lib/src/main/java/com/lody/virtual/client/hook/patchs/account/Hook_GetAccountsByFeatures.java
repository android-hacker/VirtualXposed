package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import android.accounts.IAccountManagerResponse;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAccountsByFeatures(IAccountManagerResponse,
 *      String, String[], String)
 *
 */

public class Hook_GetAccountsByFeatures extends Hook {

	@Override
	public String getName() {
		return "getAccountsByFeatures";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		IAccountManagerResponse response = (IAccountManagerResponse) args[0];
		String accountType = (String) args[1];
		String[] features = (String[]) args[2];
		LocalAccountManager.getInstance().getAccountsByFeatures(response, accountType, features);
		return 0;
	}
}
