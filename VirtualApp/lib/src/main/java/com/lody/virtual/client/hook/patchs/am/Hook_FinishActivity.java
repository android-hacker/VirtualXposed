package com.lody.virtual.client.hook.patchs.am;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class Hook_FinishActivity extends Hook {
    @Override
    public String getName() {
        return "finishActivity";
    }

    @Override
    public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
        return result;
    }
}
