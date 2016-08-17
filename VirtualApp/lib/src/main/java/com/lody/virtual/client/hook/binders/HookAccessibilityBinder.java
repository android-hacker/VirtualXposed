package com.lody.virtual.client.hook.binders;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;
import android.view.accessibility.IAccessibilityManager;

import com.lody.virtual.client.hook.base.HookBinder;

/**
 * @author Lody
 */

public class HookAccessibilityBinder extends HookBinder<IAccessibilityManager> {
    @Override
    protected IBinder queryBaseBinder() {
        return ServiceManager.getService(Context.ACCESSIBILITY_SERVICE);
    }

    @Override
    protected IAccessibilityManager createInterface(IBinder baseBinder) {
        return IAccessibilityManager.Stub.asInterface(baseBinder);
    }
}
