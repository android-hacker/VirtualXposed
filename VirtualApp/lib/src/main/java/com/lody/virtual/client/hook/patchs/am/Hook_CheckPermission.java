package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.app.ActivityManagerNative#checkPermission(String, int, int)
 *
 */

public class Hook_CheckPermission extends Hook<ActivityManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_CheckPermission(ActivityManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "checkPermission";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return method.invoke(who, args);
    }
}
