package com.lody.virtual.client.hook.patchs.account;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VAccountManager;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAuthenticatorTypes(int)
 *
 */

public class Hook_GetAuthenticatorTypes extends Hook {

	@Override
	public String getName() {
		return "getAuthenticatorTypes";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		return VAccountManager.getInstance().getAuthenticatorTypes();
	}
}
