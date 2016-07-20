package com.lody.virtual.client.hook.patchs.account;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAccountsAsUser(String, int, String)
 *
 */

public class Hook_GetAccountsAsUser extends Hook_GetAccounts {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetAccountsAsUser(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getAccountsAsUser";
    }

}
