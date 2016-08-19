package com.lody.virtual.client.hook.patchs.accessibility;

import android.content.Context;
import android.os.ServiceManager;
import android.view.accessibility.IAccessibilityManager;

import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookAccessibilityBinder;

/**
 * @author Lody
 */

public class AccessibilityPatch extends PatchObject<IAccessibilityManager, HookAccessibilityBinder> {
    @Override
    protected HookAccessibilityBinder initHookObject() {
        return new HookAccessibilityBinder();
    }

    @Override
    public void inject() throws Throwable {
        getHookObject().injectService(Context.ACCESSIBILITY_SERVICE);
    }

    @Override
    protected void applyHooks() {
        super.applyHooks();
    }

    @Override
    public boolean isEnvBad() {
        return getHookObject() != ServiceManager.getService(Context.ACCESSIBILITY_SERVICE);
    }
}
