package com.lody.virtual.client.hook.patchs.mount;

import android.os.Build;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.io.File;
import java.lang.reflect.Method;

/**
 * @author Lody
 */
/* package */ class Mkdirs extends Hook {

    @Override
    public String getName() {
        return "mkdirs";
    }

    @Override
    public boolean beforeCall(Object who, Method method, Object... args) {
        HookUtils.replaceFirstAppPkg(args);
        return super.beforeCall(who, method, args);
    }

    @Override
    public Object call(Object who, Method method, Object... args) throws Throwable {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return super.call(who, method, args);
        }
        String path;
        if (args.length == 1) {
            path = (String) args[0];
        } else {
            path = (String) args[1];
        }
        File file = new File(path);
        return file.exists() && file.mkdirs() ? 0 : -1;
    }
}
