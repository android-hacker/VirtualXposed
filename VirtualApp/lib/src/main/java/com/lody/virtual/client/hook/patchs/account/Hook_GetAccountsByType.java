package com.lody.virtual.client.hook.patchs.account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *  Account[] getAccountsByType(String type);
 */

public class Hook_GetAccountsByType extends Hook {

    @Override
    public String getName() {
        return "getAccountsByType";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        String type = (String) args[0];
        return LocalAccountManager.getInstance().getAccounts(type);
    }
}
