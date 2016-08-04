package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalServiceManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.app.IActivityManager#getServices(int, int)
 *
 */
public class Hook_GetServices extends Hook {
    @Override
    public String getName() {
        return "getServices";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        int maxNum = (int) args[0];
        int flags = (int) args[1];
        return LocalServiceManager.getInstance().getServices(maxNum, flags);
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
