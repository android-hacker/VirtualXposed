package com.lody.virtual.client.hook.patchs.account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAccountsByTypeForPackage(String, String, String)
 *
 */

public class Hook_GetAccountTypeForPackage extends Hook<AccountManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetAccountTypeForPackage(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getAccountsByTypeForPackage";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        String type = (String) args[0];
        String packageName = (String) args[1];
        return LocalAccountManager.getInstance().getAccounts(type);
    }
}
