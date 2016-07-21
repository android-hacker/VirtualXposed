package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#hasFeatures(IAccountManagerResponse, Account, String[], String)
 *
 */

public class Hook_HasFeatures extends Hook {

    @Override
    public String getName() {
        return "hasFeatures";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        IAccountManagerResponse response = (IAccountManagerResponse) args[0];
        Account account = (Account) args[1];
        String[] features = (String[]) args[2];
        LocalAccountManager.getInstance().hasFeatures(response, account, features);
        return 0;
    }
}
