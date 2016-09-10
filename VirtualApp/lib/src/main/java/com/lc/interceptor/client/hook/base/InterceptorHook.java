package com.lc.interceptor.client.hook.base;


import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.PatchDelegate;

import java.lang.reflect.Method;

/**
 * Created by lichen:) on 2016/9/1.
 */
public abstract class InterceptorHook extends Hook {

    /**
     *
     * @return 返回true 代表 完全消费 不执行 原有方法
     */
    public boolean isOnHookConsumed(){
        return true;
    }

    public boolean isOnHookEnabled(){
        return  true;
    }

    @Override
    public boolean isEnable() {
        return true;
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        return super.call(who, method, args);
    }

    abstract public Class<? extends PatchDelegate> getDelegatePatch();

    public boolean replaceOriginal() {
        return true;
    }
}
