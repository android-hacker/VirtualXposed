package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class Hook_GetPackageForIntentSender extends Hook {
    @Override
    public String getName() {
        return "getPackageForIntentSender";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        // FixMe
        return super.onHook(who, method, args);
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
