package com.lody.virtual.client.hook.patchs.libcore;

import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Method;

class Getpwnam extends Hook {
        @Override
        public String getName() {
            return "getpwnam";
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            if (result != null) {
                Reflect pwd = Reflect.on(result);
                int uid = pwd.get("pw_uid");
                if (uid == VirtualCore.get().myUid()) {
                    pwd.set("pw_uid", VClientImpl.get().getVUid());
                }
            }
            return result;
        }
    }
