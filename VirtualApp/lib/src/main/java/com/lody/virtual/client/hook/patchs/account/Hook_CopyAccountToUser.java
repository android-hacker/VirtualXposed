package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#copyAccountToUser(IAccountManagerResponse, Account, int, int)
 *
 */

public class Hook_CopyAccountToUser extends Hook {

    @Override
    public String getName() {
        return "copyAccountToUser";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return 0;
    }
}
