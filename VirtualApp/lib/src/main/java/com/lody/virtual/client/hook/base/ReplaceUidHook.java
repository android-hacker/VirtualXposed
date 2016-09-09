package com.lody.virtual.client.hook.base;

import android.os.Process;

import java.lang.reflect.Method;

public class ReplaceUidHook extends StaticHook {

        private final int index;
        public ReplaceUidHook(String name, int index) {
            super(name);
            this.index = index;
        }

    @Override
    public boolean beforeCall(Object who, Method method, Object... args) {
        int uid = (int) args[index];
        if (uid == Process.myUid()) {
            args[index] = getRealUid();
        }
        return super.beforeCall(who, method, args);
    }
}