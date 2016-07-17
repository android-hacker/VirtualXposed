package com.lody.virtual.client.hook.patchs.account;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalAccountManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.accounts.IAccountManager#invalidateAuthToken(String, String)
 *
 */

public class Hook_InvalidateAuthToken extends Hook<AccountManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_InvalidateAuthToken(AccountManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "invalidateAuthToken";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        String accountType = (String) args[0];
        String authToken = (String) args[1];
        LocalAccountManager.getInstance().invalidateAuthToken(accountType, authToken);
        return 0;
    }
}
