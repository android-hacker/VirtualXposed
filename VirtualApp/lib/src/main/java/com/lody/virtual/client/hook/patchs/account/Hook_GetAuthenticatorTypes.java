package com.lody.virtual.client.hook.patchs.account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAuthenticatorTypes(int)
 *
 */

public class Hook_GetAuthenticatorTypes extends Hook<AccountManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetAuthenticatorTypes(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getAuthenticatorTypes";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return LocalAccountManager.getInstance().getAuthenticatorTypes();
    }
}
