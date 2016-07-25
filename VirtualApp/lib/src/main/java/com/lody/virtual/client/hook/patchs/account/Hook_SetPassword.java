package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#setPassword(Account, String)
 *
 */

public class Hook_SetPassword extends Hook {

    @Override
    public String getName() {
        return "setPassword";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        Account account = (Account) args[0];
        String password = (String) args[1];
        LocalAccountManager.getInstance().setPassword(account, password);
        return 0;
    }
}
