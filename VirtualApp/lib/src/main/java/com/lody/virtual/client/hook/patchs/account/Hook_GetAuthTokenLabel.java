package com.lody.virtual.client.hook.patchs.account;

import android.accounts.IAccountManagerResponse;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAuthTokenLabel(IAccountManagerResponse, String, String)
 *
 */

public class Hook_GetAuthTokenLabel extends Hook {

    @Override
    public String getName() {
        return "getAuthTokenLabel";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        IAccountManagerResponse response = (IAccountManagerResponse) args[0];
        String authType = (String) args[1];
        String authTokenType = (String) args[2];
        LocalAccountManager.getInstance().getAuthTokenLabel(response, authType, authTokenType);
        return 0;
    }
}
