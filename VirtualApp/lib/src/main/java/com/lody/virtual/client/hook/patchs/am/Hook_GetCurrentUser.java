package com.lody.virtual.client.hook.patchs.am;

import android.content.pm.UserInfo;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class Hook_GetCurrentUser extends Hook<ActivityManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_GetCurrentUser(ActivityManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "getCurrentUser";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        try {
            return new UserInfo(0, "user", UserInfo.FLAG_PRIMARY);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
