package com.lody.virtual.client.hook.patchs.account;

import android.accounts.IAccountManagerResponse;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#getAccountsByFeatures(IAccountManagerResponse, String, String[], String)
 *
 */

public class Hook_GetAccountsByFeatures extends Hook<AccountManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetAccountsByFeatures(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getAccountsByFeatures";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        IAccountManagerResponse response = (IAccountManagerResponse) args[0];
        String accountType = (String) args[1];
        String[] features = (String[]) args[2];
        LocalAccountManager.getInstance().getAccountsByFeatures(response, accountType, features);
        return 0;
    }
}
