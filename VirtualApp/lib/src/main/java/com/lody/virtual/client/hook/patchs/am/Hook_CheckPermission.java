package com.lody.virtual.client.hook.patchs.am;

import android.Manifest;
import android.content.pm.PackageManager;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.app.ActivityManagerNative#checkPermission(String, int, int)
 *
 */

public class Hook_CheckPermission extends Hook<ActivityManagerPatch> {

    /**
     * 这个构造器必须有,用于依赖注入.
     *
     * @param patchObject 注入对象
     */
    public Hook_CheckPermission(ActivityManagerPatch patchObject) {
        super(patchObject);
    }

    @Override
    public String getName() {
        return "checkPermission";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        String permission = (String) args[0];
        if (Manifest.permission.ACCOUNT_MANAGER.equals(permission)) {
            return PackageManager.PERMISSION_GRANTED;
        }
        if ("com.google.android.providers.settings.permission.WRITE_GSETTINGS".equals(permission)) {
            return PackageManager.PERMISSION_GRANTED;
        }
        return method.invoke(who, args);
    }

    @Override
    public boolean isEnable() {
        return isAppProcess() || isServiceProcess();
    }
}
