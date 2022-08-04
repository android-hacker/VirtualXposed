package com.lody.virtual.client.hook.proxies.pm;

import android.content.Context;
import android.content.pm.LauncherApps;
import android.os.IInterface;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgMethodProxy;

/**
 * @author weishu
 * @date 2018/7/4.
 */
public class LauncherAppsStub extends BinderInvocationProxy {

    public LauncherAppsStub() {
        super(getInterface(), Context.LAUNCHER_APPS_SERVICE);
    }

    private static IInterface getInterface() {
        LauncherApps cm = (LauncherApps) VirtualCore.get().getContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        return mirror.android.content.pm.LauncherApps.mService.get(cm);
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        LauncherApps cm = (LauncherApps) VirtualCore.get().getContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
        mirror.android.content.pm.LauncherApps.mService.set(cm, getInvocationStub().getProxyInterface());
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new ReplaceCallingPkgMethodProxy("addOnAppsChangedListener"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getLauncherActivities"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("resolveActivity"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("startActivityAsUser"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("showAppDetailsAsUser"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("isPackageEnabled"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("isActivityEnabled"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getApplicationInfo"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("pinShortcuts"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("startShortcut"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcutIconResId"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcutIconFd"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("hasShortcutHostPermission"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcutConfigActivities"));
        addMethodProxy(new ReplaceCallingPkgMethodProxy("getShortcutConfigActivityIntent"));
    }
}
