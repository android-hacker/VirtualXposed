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
 * @see android.accounts.IAccountManager#addAccount(IAccountManagerResponse, String, String, String[], boolean, Bundle)
 *
 */

public class Hook_AddAccount extends Hook {


    @Override
    public String getName() {
        return "addAccount";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        if (args[0] instanceof Account) {
            Account account = (Account) args[0];
            String password = (String) args[1];
            Bundle userdata = (Bundle) args[2];
            return LocalAccountManager.getInstance().addAccount(account, password, userdata);
        } else {
            IAccountManagerResponse response = (IAccountManagerResponse) args[0];
            String accountType = (String) args[1];
            String authTokenType = (String) args[2];
            String[] requiredFeatures = (String[]) args[3];
            boolean expectActivityLaunch = (boolean) args[4];
            Bundle options = (Bundle) args[5];
            LocalAccountManager.getInstance().addAcount(response, accountType, authTokenType, requiredFeatures, expectActivityLaunch, options);
            return 0;
        }
    }
}
