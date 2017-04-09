package com.lody.virtual.client.hook.proxies.bluetooth;

import android.os.Build;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;

import java.lang.reflect.Method;

import mirror.android.bluetooth.IBluetooth;

/**
 * @see android.bluetooth.BluetoothManager
 */
public class BluetoothStub extends BinderInvocationProxy {
    public static final String SERVICE_NAME = Build.VERSION.SDK_INT >= 17 ?
            "bluetooth_manager" :
            "bluetooth";

    public BluetoothStub() {
        super(IBluetooth.Stub.asInterface, SERVICE_NAME);
    }

    @Override
    protected void onBindHooks() {
        super.onBindHooks();
        addMethodProxy(new GetAddress());
    }

    private static class GetAddress extends StaticMethodProxy {

        GetAddress() {
            super("getAddress");
        }

        @Override
        public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
            if (VirtualCore.get().getPhoneInfoDelegate() != null) {
                String res = VirtualCore.get().getPhoneInfoDelegate().getBluetoothAddress((String) result, getAppUserId());
                if (res != null) {
                    return res;
                }
            }
            return super.afterCall(who, method, args, result);
        }
    }
}
