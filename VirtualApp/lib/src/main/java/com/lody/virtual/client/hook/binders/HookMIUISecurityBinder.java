package com.lody.virtual.client.hook.binders;

import android.os.IBinder;
import android.os.ServiceManager;

import com.lody.virtual.client.hook.base.HookBinder;

import miui.security.ISecurityManager;

/**
 * @author Lody
 */

public class HookMIUISecurityBinder extends HookBinder<ISecurityManager> {

    public static final String SECURITY_SERVICE = "security";

    @Override
    protected IBinder queryBaseBinder() {
        return ServiceManager.getService(SECURITY_SERVICE);
    }

    @Override
    protected ISecurityManager createInterface(IBinder baseBinder) {
        return ISecurityManager.Stub.asInterface(baseBinder);
    }
}
