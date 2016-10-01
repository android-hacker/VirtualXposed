package com.lody.virtual.client.hook.patchs.libcore;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import mirror.libcore.io.Os;

/**
 * @author Lody
 */
public class Stat extends Hook {

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
    public String getName() {
        return "stat";
    }
}
