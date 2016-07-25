package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getPassword(Account)
 *
 */

public class Hook_GetPassword extends Hook {

    @Override
    public String getName() {
        return "getPassword";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        Account account = (Account) args[0];
        return LocalAccountManager.getInstance().getPassword(account);
    }
}
