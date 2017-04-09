package com.lody.virtual.client.hook.base;

import android.os.Process;

import com.lody.virtual.helper.utils.ArrayUtils;

import java.lang.reflect.Method;

public class ReplaceLastUidMethodProxy extends StaticMethodProxy {

    public ReplaceLastUidMethodProxy(String name) {
        super(name);
    }

    @Override
    public boolean beforeCall(Object who, Method method, Object... args) {
        int index = ArrayUtils.indexOfLast(args, Integer.class);
        if (index != -1) {
            int uid = (int) args[index];
            if (uid == Process.myUid()) {
                args[index] = getRealUid();
            }
        }
        return super.beforeCall(who, method, args);
    }
}