package com.lody.virtual.client.hook.proxies.window;

import android.content.Context;
import android.os.Build;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.Inject;
import com.lody.virtual.client.hook.base.StaticMethodProxy;

import mirror.android.view.Display;
import mirror.android.view.IWindowManager;
import mirror.android.view.WindowManagerGlobal;
import mirror.com.android.internal.policy.PhoneWindow;

/**
 * @author Lody
 */
@Inject(MethodProxies.class)
public class WindowManagerStub extends BinderInvocationProxy {

    public WindowManagerStub() {
        super(IWindowManager.Stub.asInterface, Context.WINDOW_SERVICE);
    }

    @Override
    public void inject() throws Throwable {
        super.inject();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (WindowManagerGlobal.sWindowManagerService != null) {
                WindowManagerGlobal.sWindowManagerService.set(getInvocationStub().getProxyInterface());
            }
        } else {
            if (Display.sWindowManager != null) {
                Display.sWindowManager.set(getInvocationStub().getProxyInterface());
            }
        }
        if (PhoneWindow.TYPE != null) {
            PhoneWindow.sWindowManager.set(getInvocationStub().getProxyInterface());
        }
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new StaticMethodProxy("addAppToken"));
        addMethodProxy(new StaticMethodProxy("setScreenCaptureDisabled"));
    }
}
