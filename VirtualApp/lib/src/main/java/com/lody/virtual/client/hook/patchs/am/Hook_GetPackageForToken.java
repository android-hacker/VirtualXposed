package com.lody.virtual.client.hook.patchs.am;

import android.os.IBinder;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.LocalActivityManager;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * String getPackageForToken(IBinder token);
 *
 */

public class Hook_GetPackageForToken extends Hook {

    @Override
    public String getName() {
        return "getPackageForToken";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        IBinder token = (IBinder) args[0];
        String pkg = LocalActivityManager.getInstance().getPackageForToken(token);
        if (pkg != null) {
            return pkg;
        }
        return super.onHook(who, method, args);
    }
}
