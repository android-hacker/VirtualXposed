package com.lody.virtual.client.hook.proxies.devicepolicy;

import android.content.Context;
import android.util.Log;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;

import java.lang.reflect.Method;

import mirror.android.app.admin.IDevicePolicyManager;

/**
 * Created by wy on 2017/10/20.
 */

public class DevicePolicyManagerStub extends BinderInvocationProxy{
    public DevicePolicyManagerStub() {
        super(IDevicePolicyManager.Stub.asInterface, Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new GetStorageEncryptionStatus());
    }

    private static class GetStorageEncryptionStatus extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getStorageEncryptionStatus";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            args[0] = VirtualCore.get().getHostPkg();
            return method.invoke(who, args);
        }
    }
}
