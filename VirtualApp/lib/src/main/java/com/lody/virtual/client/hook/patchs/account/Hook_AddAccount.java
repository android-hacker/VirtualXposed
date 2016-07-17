package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;
import android.os.Bundle;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#addAccount(IAccountManagerResponse, String, String, String[], boolean, Bundle)
 * @see android.accounts.IAccountManager#addAccountAsUser(IAccountManagerResponse, String, String, String[], boolean, Bundle, int)
 * @see android.accounts.IAccountManager#addAccountExplicitly(Account, String, Bundle)
 */

public class Hook_AddAccount extends Hook<AccountManagerPatch> {
    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_AddAccount(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "addAccount";
    }

    @Override
    public Object onHook(final Object who, Method method, Object... args) throws Throwable {
        return method.invoke(who, args);
    }
}
