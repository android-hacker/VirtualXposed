package com.lody.virtual.client.hook.patchs.account;

import android.accounts.Account;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getPreviousName(Account)
 */

public class Hook_GetPreviousName extends Hook<AccountManagerPatch> {
    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetPreviousName(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getPreviousName";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        AccountUtils.replaceAccount(args);
        return method.invoke(who, args);
    }
}
