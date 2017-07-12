package com.lody.virtual.client.hook.proxies.connectivity;

import android.content.Context;

import com.lody.virtual.client.hook.base.BinderInvocationProxy;
import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.base.ReplaceLastPkgMethodProxy;
import com.lody.virtual.client.hook.base.StaticMethodProxy;
import com.lody.virtual.client.ipc.ServiceManagerNative;

import java.lang.reflect.Method;

import mirror.android.net.IConnectivityManager;

/**
 * @author legency
 */
public class ConnectivityStub extends BinderInvocationProxy {

    private com.lody.virtual.IConnectivityManager vService = com.lody.virtual.IConnectivityManager.Stub.asInterface(
            ServiceManagerNative.getService(ServiceManagerNative.CONNECTIVITY)
    );

    public ConnectivityStub() {
        super(IConnectivityManager.Stub.asInterface, Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onBindMethods() {
        super.onBindMethods();
        addMethodProxy(new RedirectMethodProxy("getActiveNetworkInfo"));
        addMethodProxy(new RedirectMethodProxy("getActiveNetworkInfoForUid"));
        addMethodProxy(new RedirectMethodProxy("getNetworkInfo"));
        addMethodProxy(new RedirectMethodProxy("getAllNetworkInfo"));
        addMethodProxy(new RedirectMethodProxy("isActiveNetworkMetered"));
        addMethodProxy(new RedirectMethodProxy("requestRouteToHostAddress"));
        addMethodProxy(new RedirectMethodProxy("getActiveLinkProperties"));
        addMethodProxy(new RedirectMethodProxy("getLinkProperties"));

    }

    private class RedirectMethodProxy extends StaticMethodProxy {

        public RedirectMethodProxy(String name) {
            super(name);
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            try {
                Method redirectMethod = vService.getClass().getMethod(method.getName(), method.getParameterTypes());
                return redirectMethod.invoke(vService, args);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return super.call(who, method, args);
        }
    }
}
