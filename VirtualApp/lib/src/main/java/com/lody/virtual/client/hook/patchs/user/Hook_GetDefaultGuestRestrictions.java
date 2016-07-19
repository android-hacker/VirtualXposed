package com.lody.virtual.client.hook.patchs.user;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class Hook_GetDefaultGuestRestrictions extends Hook<UserManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetDefaultGuestRestrictions(UserManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getDefaultGuestRestrictions";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return null;
    }
}
