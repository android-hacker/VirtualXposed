package com.lody.virtual.client.hook.patchs.account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

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
        return LocalAccountManager.getInstance().getAuthenticatorTypes();
    }
}
