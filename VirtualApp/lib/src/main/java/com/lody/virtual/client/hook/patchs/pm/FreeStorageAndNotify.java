package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.IPackageDataObserver;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 */
class FreeStorageAndNotify extends Hook {
    @Override
    public String getName() {
        return "freeStorageAndNotify";
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        if (args[args.length - 1] instanceof IPackageDataObserver) {
            IPackageDataObserver observer = (IPackageDataObserver) args[args.length - 1];
            observer.onRemoveCompleted(null, true);
        }
        return 0;
    }
}
