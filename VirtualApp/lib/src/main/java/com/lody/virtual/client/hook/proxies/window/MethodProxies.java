package com.lody.virtual.client.hook.proxies.window;

import android.os.IInterface;

import com.lody.virtual.client.hook.base.MethodProxy;
import com.lody.virtual.client.hook.proxies.window.session.WindowSessionPatch;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

class MethodProxies {


    static class OpenSession extends BasePatchSession {

        @Override
        public String getMethodName() {
            return "openSession";
        }
    }


    static class OverridePendingAppTransition extends BasePatchSession {

        @Override
        public String getMethodName() {
            return "overridePendingAppTransition";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (args[0] instanceof String) {
                args[0] = getHostPkg();
            }
            return super.call(who, method, args);
        }
    }


    static class OverridePendingAppTransitionInPlace extends MethodProxy {

        @Override
        public String getMethodName() {
            return "overridePendingAppTransitionInPlace";
        }

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            if (args[0] instanceof String) {
                args[0] = getHostPkg();
            }
            return method.invoke(who, args);
        }
    }


    static class SetAppStartingWindow extends BasePatchSession {

        @Override
        public String getMethodName() {
            return "setAppStartingWindow";
        }
    }

    abstract static class BasePatchSession extends MethodProxy {

        @Override
        public Object call(Object who, Method method, Object... args) throws Throwable {
            Object session = method.invoke(who, args);
            if (session instanceof IInterface) {
                return proxySession((IInterface) session);
            }
            return session;
        }

        private Object proxySession(IInterface session) {
            WindowSessionPatch windowSessionPatch = new WindowSessionPatch(session);
            return windowSessionPatch.getInvocationStub().getProxyInterface();
        }
    }
}
