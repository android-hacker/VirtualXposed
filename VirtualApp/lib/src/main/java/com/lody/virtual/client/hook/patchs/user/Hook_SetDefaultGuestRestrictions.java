package com.lody.virtual.client.hook.patchs.user;

import android.os.Bundle;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.os.IUserManager#setDefaultGuestRestrictions(Bundle)
 *
 */

public class Hook_SetDefaultGuestRestrictions extends Hook {


    @Override
    public String getName() {
        return "setDefaultGuestRestrictions";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return 0;
    }
}
