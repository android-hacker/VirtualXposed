package com.lody.virtual.client.hook.patchs.shortcut;

import com.lody.virtual.client.hook.base.PatchBinderDelegate;
import com.lody.virtual.client.hook.base.ReplaceCallingPkgHook;

import mirror.android.content.pm.ILauncherApps;

/**
 * @author Lody
 */
public class ShortcutServicePatch extends PatchBinderDelegate {


    public ShortcutServicePatch() {
        super(ILauncherApps.Stub.asInterface, "shortcut");
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addHook(new ReplaceCallingPkgHook("getManifestShortcuts"));
        addHook(new ReplaceCallingPkgHook("getDynamicShortcuts"));
        addHook(new ReplaceCallingPkgHook("setDynamicShortcuts"));
        addHook(new ReplaceCallingPkgHook("addDynamicShortcuts"));
        addHook(new ReplaceCallingPkgHook("createShortcutResultIntent"));
        addHook(new ReplaceCallingPkgHook("disableShortcuts"));
        addHook(new ReplaceCallingPkgHook("enableShortcuts"));
        addHook(new ReplaceCallingPkgHook("getRemainingCallCount"));
        addHook(new ReplaceCallingPkgHook("getRateLimitResetTime"));
        addHook(new ReplaceCallingPkgHook("getIconMaxDimensions"));
        addHook(new ReplaceCallingPkgHook("getMaxShortcutCountPerActivity"));
        addHook(new ReplaceCallingPkgHook("reportShortcutUsed"));
        addHook(new ReplaceCallingPkgHook("onApplicationActive"));
    }
}
