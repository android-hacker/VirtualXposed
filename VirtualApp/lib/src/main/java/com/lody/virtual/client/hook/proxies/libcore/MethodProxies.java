package com.lody.virtual.client.hook.proxies.libcore;

import com.lody.virtual.client.NativeEngine;
import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.helper.utils.Reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import mirror.libcore.io.Os;

/**
 * @author Lody
 */

class MethodProxies {

    static class Lstat extends Stat {

        @Override
        public String getMethodName() {
            return "lstat";
        }
    }

    static class Getpwnam extends MethodProxy {
            @Override
            public String getMethodName() {
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

    static class GetUid extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getuid";
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            int uid = (int) result;
            return NativeEngine.onGetUid(uid);
        }
    }

    static class GetsockoptUcred extends MethodProxy {
            @Override
            public String getMethodName() {
                return "getsockoptUcred";
            }

            @Override
            public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
                if (result != null) {
                    Reflect ucred = Reflect.on(result);
                    int uid = ucred.get("uid");
                    if (uid == VirtualCore.get().myUid()) {
                        ucred.set("uid", getBaseVUid());
                    }
                }
                return result;
            }
        }

    static class Stat extends MethodProxy {

        private static Field st_uid;

        static {
            try {
                Method stat = Os.TYPE.getMethod("stat", String.class);
                Class<?> StructStat = stat.getReturnType();
                st_uid = StructStat.getDeclaredField("st_uid");
                st_uid.setAccessible(true);
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            int uid = (int) st_uid.get(result);
            if (uid == VirtualCore.get().myUid()) {
                st_uid.set(result, getBaseVUid());
            }
            return result;
        }

        @Override
        public String getMethodName() {
            return "stat";
        }
    }
}
