package com.lody.virtual.client.hook.proxies.view;

import android.annotation.SuppressLint;
import android.util.Log;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.utils.MethodParameterUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import mirror.android.view.IAutoFillManager;

/**
 * @author 陈磊.
 */

public class AutoFillManagerStub extends BinderInvocationProxy {

    private static final String TAG = "AutoFillManagerStub";

    private static final String AUTO_FILL_NAME = "autofill";
    public AutoFillManagerStub() {
        super(IAutoFillManager.Stub.asInterface, AUTO_FILL_NAME);
    }

    @SuppressLint("WrongConstant")
    @Override
    public void inject() throws Throwable {
        super.inject();
        try {
            Object AutoFillManagerInstance = getContext().getSystemService(AUTO_FILL_NAME);
            if (AutoFillManagerInstance == null) {
                throw new NullPointerException("AutoFillManagerInstance is null.");
            }
            Object AutoFillManagerProxy = getInvocationStub().getProxyInterface();
            if (AutoFillManagerProxy == null) {
                throw new NullPointerException("AutoFillManagerProxy is null.");
            }
            Field AutoFillManagerServiceField = AutoFillManagerInstance.getClass().getDeclaredField("mService");
            AutoFillManagerServiceField.setAccessible(true);
            AutoFillManagerServiceField.set(AutoFillManagerInstance, AutoFillManagerProxy);
        } catch (Throwable tr) {
            Log.e(TAG, "AutoFillManagerStub inject error.", tr);
            return;
        }
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "startSession";
            }
            @Override
            public boolean beforeCall(Object who, Method method, Object... args) {
                MethodParameterUtils.replaceLastAppPkg(args);
                return super.beforeCall(who, method, args);
            }
        });
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "updateOrRestartSession";
            }
            @Override
            public boolean beforeCall(Object who, Method method, Object... args) {
                MethodParameterUtils.replaceLastAppPkg(args);
                return super.beforeCall(who, method, args);
            }
        });
        addMethodProxy(new MethodProxy() {
            @Override
            public String getMethodName() {
                return "isServiceEnabled";
            }
            @Override
            public boolean beforeCall(Object who, Method method, Object... args) {
                MethodParameterUtils.replaceLastAppPkg(args);
                return super.beforeCall(who, method, args);
            }
        });
    }

}
