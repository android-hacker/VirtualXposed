package com.lody.virtual.client.hook.proxies.launcher_apps;

import android.annotation.TargetApi;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;

import mirror.android.content.pm.ILauncherApps;

/**
 * @author Lody
 */
@TargetApi(25/*Oreo*/)
public class LauncherAppsStub extends BinderInvocationProxy {


    public LauncherAppsStub() {
        super(ILauncherApps.Stub.asInterface, "launcherapps");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("startActivityAsUser"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("showAppDetailsAsUser"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("resolveActivity"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("addOnAppsChangedListener"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("pinShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("isPackageEnabled"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("isActivityEnabled"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("hasShortcutHostPermission"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcutIconFd"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcutConfigActivities"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcutConfigActivityIntent"));
    }
}
