package com.lody.virtual.helper.ipcbus;

import android.os.Binder;

import com.lody.virtual.helper.collection.SparseArray;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 */
public class ServerInterface {

    private Class<?> interfaceClass;
    private final SparseArray<IPCMethod> codeToInterfaceMethod;
    private final Map<Method, IPCMethod> methodToIPCMethodMap;

    public ServerInterface(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
        Method[] methods = interfaceClass.getMethods();
        codeToInterfaceMethod = new SparseArray<>(methods.length);
        methodToIPCMethodMap = new HashMap<>(methods.length);
        for (int i = 0; i < methods.length; i++) {
            int code = Binder.FIRST_CALL_TRANSACTION + i;
            IPCMethod ipcMethod = new IPCMethod(code, methods[i], interfaceClass.getName());
            codeToInterfaceMethod.put(code, ipcMethod);
            methodToIPCMethodMap.put(methods[i], ipcMethod);
        }
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public String getInterfaceName() {
        return interfaceClass.getName();
    }

    public IPCMethod getIPCMethod(int code) {
        return codeToInterfaceMethod.get(code);
    }

    public IPCMethod getIPCMethod(Method method) {
        return methodToIPCMethodMap.get(method);
    }
}
