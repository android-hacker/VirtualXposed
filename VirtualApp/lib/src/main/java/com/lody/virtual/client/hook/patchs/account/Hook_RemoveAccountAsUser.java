package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;
import android.accounts.IAccountManagerResponse;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#removeAccountAsUser(IAccountManagerResponse, Account, boolean, int)
 *
 */

public class Hook_RemoveAccountAsUser extends Hook_RemoveAccount {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_RemoveAccountAsUser(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "removeAccountAsUser";
    }

}
