package com.lody.virtual.client.hook.patchs.libcore;

import com.lody.virtual.client.hook.base.HookDelegate;
import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchDelegate;
import com.lody.virtual.client.hook.base.ReplaceUidHook;

import mirror.libcore.io.ForwardingOs;
import mirror.libcore.io.Libcore;

/**
 * @author Lody
 */
@Patch({
        GetUid.class,
        Stat.class,
        Lstat.class,
        Getpwnam.class,
        GetsockoptUcred.class,
})
public class LibCorePatch extends PatchDelegate<HookDelegate<Object>> {

    public LibCorePatch() {
        super(new HookDelegate<Object>(getOs()));
    }

    private static Object getOs() {
        Object os = Libcore.os.get();
        if (ForwardingOs.os != null) {
            Object posix = ForwardingOs.os.get(os);
            if (posix != null) {
                os = posix;
            }
        }
        return os;
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new ReplaceUidHook("chown", 1));
        addHook(new ReplaceUidHook("fchown", 1));
        addHook(new ReplaceUidHook("getpwuid", 0));
        addHook(new ReplaceUidHook("lchown", 1));
        addHook(new ReplaceUidHook("setuid", 0));
    }

    @Override
    public void inject() throws Throwable {
        Libcore.os.set(getHookDelegate().getProxyInterface());
    }

    @Override
    public boolean isEnvBad() {
        return getOs() != getHookDelegate().getProxyInterface();
    }
}
