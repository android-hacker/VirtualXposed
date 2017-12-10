package com.lody.virtual.helper.ipcbus;

/**
 * @author Lody
 */
public class IPCSingleton<T> {

    private Class<?> ipcClass;
    private T instance;

    public IPCSingleton(Class<?> ipcClass) {
        this.ipcClass = ipcClass;
    }

    public T get() {
        if (instance == null) {
            synchronized (this) {
                if (instance == null) {
                    instance = IPCBus.get(ipcClass);
                }
            }
        }
        return instance;
    }

}
