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
 * @see android.accounts.IAccountManager#confirmCredentialsAsUser(IAccountManagerResponse, Account, Bundle, boolean, int)
 *
 */

public class Hook_ConfirmCredentialsAsUser extends Hook<AccountManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_ConfirmCredentialsAsUser(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "confirmCredentialsAsUser";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        IAccountManagerResponse response = (IAccountManagerResponse) args[0];
        Account account = (Account) args[1];
        Bundle options = (Bundle) args[2];
        boolean expectActivityLaunch = (boolean) args[3];
        LocalAccountManager.getInstance().confirmCredentials(response, account, options, expectActivityLaunch);
        return 0;
    }
}
