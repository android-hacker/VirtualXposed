package com.lody.virtual.client.hook.patchs.camera;

import android.hardware.ICameraClient;
import android.hardware.camera2.utils.BinderHolder;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.hardware.ICameraService#connect(ICameraClient, int, String, int, BinderHolder)
 *
 */

public class Hook_ConnectLegacy extends Hook {


    @Override
    public String getName() {
        return "connectLegacy";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        HookUtils.replaceAppPkg(args);
        return method.invoke(who, args);
    }
}
