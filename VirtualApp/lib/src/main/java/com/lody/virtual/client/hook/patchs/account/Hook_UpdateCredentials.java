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
 * @see android.accounts.IAccountManager#updateCredentials(IAccountManagerResponse, Account, String, boolean, Bundle)
 *
 */

public class Hook_UpdateCredentials extends Hook<AccountManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_UpdateCredentials(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "updateCredentials";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        IAccountManagerResponse response = (IAccountManagerResponse) args[0];
        Account account = (Account) args[1];
        String authTokenType = (String) args[2];
        boolean expectActivityLaunch = (boolean) args[3];
        Bundle options = (Bundle) args[4];
        LocalAccountManager.getInstance().updateCredentials(response, account, authTokenType, expectActivityLaunch, options);
        return 0;
    }
}
