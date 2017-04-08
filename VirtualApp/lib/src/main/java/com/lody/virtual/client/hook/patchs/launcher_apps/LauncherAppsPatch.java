package com.lody.virtual.client.hook.patchs.launcher_apps;

import android.annotation.TargetApi;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;

import mirror.android.content.pm.ILauncherApps;

/**
 * @author Lody
 */
@TargetApi(25/*Oreo*/)
public class LauncherAppsPatch extends PatchBinderDelegate {


    public LauncherAppsPatch() {
        /*
          Are you kidding me?
         */
        super(ILauncherApps.Stub.asInterface, ">(TTKey;)TT;");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new ReplaceCallingPkgHook("startActivityAsUser"));
        addHook(new ReplaceCallingPkgHook("showAppDetailsAsUser"));
        addHook(new ReplaceCallingPkgHook("resolveActivity"));
        addHook(new ReplaceCallingPkgHook("addOnAppsChangedListener"));
        addHook(new ReplaceCallingPkgHook("pinShortcuts"));
        addHook(new ReplaceCallingPkgHook("isPackageEnabled"));
        addHook(new ReplaceCallingPkgHook("isActivityEnabled"));
        addHook(new ReplaceCallingPkgHook("hasShortcutHostPermission"));
        addHook(new ReplaceCallingPkgHook("getShortcuts"));
        addHook(new ReplaceCallingPkgHook("getShortcutIconFd"));
        addHook(new ReplaceCallingPkgHook("getShortcutConfigActivities"));
        addHook(new ReplaceCallingPkgHook("getShortcutConfigActivityIntent"));
    }
}
