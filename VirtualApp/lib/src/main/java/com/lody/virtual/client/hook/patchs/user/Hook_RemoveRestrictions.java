package com.lody.virtual.client.hook.patchs.user;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class Hook_RemoveRestrictions extends Hook {


    @Override
    public String getName() {
        return "removeRestrictions";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return 0;
    }
}
