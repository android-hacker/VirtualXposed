package com.lody.virtual.client.hook.patchs.account;

import android.accounts.IAccountManagerResponse;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#editProperties(IAccountManagerResponse, String, boolean)
 *
 */

public class Hook_EditProperties extends Hook {

    @Override
    public String getName() {
        return "editProperties";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        IAccountManagerResponse response = (IAccountManagerResponse) args[0];
        String accountType = (String) args[1];
        boolean expectActivityLaunch = (boolean) args[2];
        LocalAccountManager.getInstance().editProperties(response, accountType, expectActivityLaunch);
        return 0;
    }
}
