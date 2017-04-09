package com.lody.virtual.client.hook.proxies.wifi;

import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.utils.MethodParameterUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

class MethodProxies {

    static class GetBatchedScanResults extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getBatchedScanResults";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    static class GetScanResults extends MethodProxy {

        @Override
        public String getMethodName() {
            return "getScanResults";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }

    static class SetWifiEnabled extends MethodProxy {

        @Override
        public String getMethodName() {
            return "setWifiEnabled";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            MethodParameterUtils.replaceFirstAppPkg(args);
            return method.invoke(who, args);
        }
    }
}
