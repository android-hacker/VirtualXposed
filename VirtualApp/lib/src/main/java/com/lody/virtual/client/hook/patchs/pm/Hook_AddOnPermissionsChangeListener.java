package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class Hook_AddOnPermissionsChangeListener extends Hook<PackageManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_AddOnPermissionsChangeListener(PackageManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "addOnPermissionsChangeListener";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return 0;
    }
}
