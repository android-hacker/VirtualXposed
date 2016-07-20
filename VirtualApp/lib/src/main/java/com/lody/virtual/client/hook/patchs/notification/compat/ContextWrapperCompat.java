package com.lody.virtual.client.hook.patchs.notification.compat;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;

/**
 * Created by 247321453 on 2016/7/17.
 */
public class ContextWrapperCompat extends ContextWrapper {
    private Context pluginContext;

    public ContextWrapperCompat(Context base, String packageName) {
        super(base);

        try {
            pluginContext = base.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY | Context.CONTEXT_INCLUDE_CODE);
        } catch (PackageManager.NameNotFoundException e) {
            pluginContext = base;
        }
    }

    @Override
    public Context getBaseContext() {
        return super.getBaseContext();
    }

    public ContextWrapperCompat(Context base, Context inflationContext) {
        super(base);
        this.pluginContext = inflationContext;
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return pluginContext.getApplicationInfo();
    }

    @Override
    public Context getApplicationContext() {
        return pluginContext.getApplicationContext();
    }

    @Override
    public Resources getResources() {
        return pluginContext.getResources();
    }

    @Override
    public Resources.Theme getTheme() {
        return pluginContext.getTheme();
    }

    @Override
    public String getPackageName() {
        return pluginContext.getPackageName();
    }
}
