package com.lody.virtual.client.hook.patchs.account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAccountsAsUser(String, int, String)
 *
 */

public class Hook_GetAccountsAsUser extends Hook_GetAccounts {


    @Override
    public String getName() {
        return "getAccountsAsUser";
    }

}
