package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getUserData(Account, String)
 */

public class Hook_GetUserData extends Hook<AccountManagerPatch> {
    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetUserData(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getUserData";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        //FIXME: Sha ka la ka
        return method.invoke(who, args);
    }
}
