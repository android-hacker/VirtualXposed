package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class Hook_AddOnPermissionsChangeListener extends Hook {


    @Override
    public String getName() {
        return "addOnPermissionsChangeListener";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return 0;
    }
}
