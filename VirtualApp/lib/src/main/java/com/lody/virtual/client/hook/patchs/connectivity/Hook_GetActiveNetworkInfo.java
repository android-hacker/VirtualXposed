package com.lody.virtual.client.hook.patchs.connectivity;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * Created by legency on 2016/8/19.
 */
public class Hook_GetActiveNetworkInfo extends Hook {
    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        HookUtils.replaceLastAppPkg(args);
        return method.invoke(who, args);
    }

    @Override
    public String getName() {
        return "getActiveNetworkInfo";
    }
}
