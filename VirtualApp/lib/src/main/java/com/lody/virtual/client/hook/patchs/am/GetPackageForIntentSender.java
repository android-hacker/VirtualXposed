package com.lody.virtual.client.hook.patchs.am;

import android.os.IInterface;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */

class GetPackageForIntentSender extends Hook {
    @Override
    public String getName() {
        return "getPackageForIntentSender";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        IInterface sender = (IInterface) args[0];
        if (sender != null) {
            String packageName = VActivityManager.get().getPackageForIntentSender(sender.asBinder());
            if (packageName != null) {
                return packageName;
            }
        }
        return super.call(who, method, args);
    }

    @Override
    public boolean isEnable() {
        return isAppProcess();
    }
}
