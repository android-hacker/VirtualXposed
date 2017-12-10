package com.lody.virtual.helper.ipcbus;


import android.os.IBinder;

import java.lang.reflect.Proxy;

/**
 * @author Lody
 */
public class IPCBus {

    private static IServerCache sCache;

    public static void initialize(IServerCache cache) {
        sCache = cache;
    }

    private static void checkInitialized() {
        if (sCache == null) {
            throw new IllegalStateException("please call initialize() at first.");
        }
    }

    public static void register(Class<?> interfaceClass, Object server) {
        checkInitialized();
        ServerInterface serverInterface = new ServerInterface(interfaceClass);
        TransformBinder binder = new TransformBinder(serverInterface, server);
        sCache.join(serverInterface.getInterfaceName(), binder);
    }

    public static <T> T get(Class<?> interfaceClass) {
        checkInitialized();
        ServerInterface serverInterface = new ServerInterface(interfaceClass);
        IBinder binder = sCache.query(serverInterface.getInterfaceName());
        if (binder == null) {
            return null;
        }
        //noinspection unchecked
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, new IPCInvocationBridge(serverInterface, binder));
    }
}
