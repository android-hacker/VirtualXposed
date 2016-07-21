package com.lody.virtual.client.hook.patchs.user;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class Hook_GetProfileParent extends Hook {


    @Override
    public String getName() {
        return "getProfileParent";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return null;
    }
}
