package com.lody.virtual.client.hook.patchs.account;

/**
 * @author Lody
 */

public class Hook_AddAccountAsUser extends Hook_AddAccount {
    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_AddAccountAsUser(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "addAccountAsUser";
    }
}
