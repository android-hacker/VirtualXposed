package com.lody.virtual.client.hook.patchs.libcore;

import com.lody.virtual.IOHook;
import com.lody.virtual.client.VClientImpl;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.base.HookObject;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.ReflectException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import libcore.io.ForwardingOs;
import libcore.io.Libcore;
import libcore.io.Os;

/**
 * @author Lody
 */

public class LibCorePatch extends PatchObject<Os,HookObject<Os>> {

    private Field st_uid;

    public LibCorePatch() {
        try {
            Method stat = Os.class.getMethod("stat", String.class);
            Class<?> StructStat = stat.getReturnType();
            st_uid = StructStat.getDeclaredField("st_uid");
            st_uid.setAccessible(true);
        } catch (Throwable e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    protected HookObject<Os> initHookObject() {
        return new HookObject<Os>(getOs(), Os.class);
    }

    @Override
    public void inject() throws Throwable {
        boolean fail = true;
        try {
            Libcore.os = getHookObject().getProxyObject();
            fail = false;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (fail) {
            try {
                Reflect.on(Libcore.class).set("os", getHookObject().getProxyObject());
            } catch (ReflectException ops) {
                ops.printStackTrace();
            }
        }
    }

    @Override
    protected void applyHooks() {
        super.applyHooks();
        addHook(new getuid());
        addHook(new stat());
    }

    private final class getuid extends Hook {
        @Override
        public String getName() {
            return "getuid";
        }

        @Override
        public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
            int uid = (int) result;
            return IOHook.onGetUid(uid);
        }
    }

    private final class stat extends Hook {
        @Override
        public String getName() {
            return "stat";
        }

        @Override
        public Object afterHook(Object who, Method method, Object[] args, Object result) throws Throwable {
            int uid = (int) st_uid.get(result);
            if (uid == VirtualCore.getCore().myUid()) {
                st_uid.set(result, VClientImpl.getClient().getVUid());
            }
            return result;
        }
    }

    @Override
    public boolean isEnvBad() {
        return getOs() != getHookObject().getProxyObject();
    }

    public Os getOs() {
        Os os = Libcore.os;
        if (os instanceof ForwardingOs) {
            try {
                return Reflect.on(os).get("os");
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return os;
    }
}
