package com.lody.virtual.client.hook.providers;

import android.net.Uri;
import android.os.Bundle;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.helper.utils.XLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * @author Lody
 */

public class SettingsProviderHook extends ExternalProviderHook {

    public SettingsProviderHook(Object base) {
        super(base);
    }

    @Override
    public Bundle call(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        XLog.d("#########", "call %s", Arrays.toString(args));
        if (args[1] instanceof String) {
            String methodName = (String) args[1];
            if (methodName.endsWith("secure")) {
               try {
                   return super.call(method, args);
               } catch (SecurityException e) {
                   return null;
               }
            }
        }
        return super.call(method, args);
    }

    @Override
    public Uri insert(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
        XLog.d("#########", "insert %s", Arrays.toString(args));
        return super.insert(method, args);
    }

    @Override
    protected void processArgs(Method method, Object... args) {
        if (args.length > 0 && args[0] instanceof String) {
            String pkg = (String) args[0];
            if (VirtualCore.getCore().isAppInstalled(pkg)) {
                args[0] = VirtualCore.getCore().getHostPkg();
            }
        }
    }
}
