package com.lody.virtual.client.hook.patchs.libcore;

import com.lody.virtual.client.hook.base.HookObject;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.helper.utils.Reflect;
import com.lody.virtual.helper.utils.ReflectException;

import libcore.io.ForwardingOs;
import libcore.io.Libcore;
import libcore.io.Os;

/**
 * @author Lody
 */

public class LibCorePatch extends PatchObject<Os,HookObject<Os>> {


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
