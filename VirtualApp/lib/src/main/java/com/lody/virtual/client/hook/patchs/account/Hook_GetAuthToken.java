package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAuthToken(IAccountManagerResponse, Account, String, boolean, boolean, Bundle)
 *
 */

public class Hook_GetAuthToken extends Hook<AccountManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetAuthToken(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getAuthToken";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        IAccountManagerResponse response = (IAccountManagerResponse) args[0];
        Account account = (Account) args[1];
        String authTokenType = (String) args[2];
        boolean notifyOnAuthFailure = (boolean) args[3];
        boolean expectActivityLaunch = (boolean) args[4];
        Bundle options = (Bundle) args[5];
        LocalAccountManager.getInstance().getAuthToken(response, account, authTokenType, notifyOnAuthFailure, expectActivityLaunch, options);
        return 0;
    }
}
