package com.lody.virtual.client.hook.base;

import android.text.TextUtils;

import com.lody.virtual.client.hook.utils.HookUtils;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 *         <p>
 *         HookHandler uses Java's {@link Proxy} to create a wrapper for existing services.
 *         <p>
 *         When any method is called on the wrapper, it checks if there is any {@link Hook} registered
 *         and enabled for that method. If so, it calls the startUniformer instead of the wrapped implementation.
 *         <p>
 *         The whole thing is managed by a {@link PatchDelegate} subclass
 */
@SuppressWarnings("unchecked")
public class HookDelegate<T> {

    private static final String TAG = HookDelegate.class.getSimpleName();

    private Map<String, Hook> internalHookTable = new HashMap<String, Hook>();
    private T mBaseInterface;
    private T mProxyInterface;


    public Map<String, Hook> getAllHooks() {
        return internalHookTable;
    }


    public HookDelegate(T baseInterface, Class<?>... proxyInterfaces) {
        this.mBaseInterface = baseInterface;
        if (baseInterface != null) {
            if (proxyInterfaces == null) {
                proxyInterfaces = HookUtils.getAllInterface(baseInterface.getClass());
            }
            mProxyInterface = (T) Proxy.newProxyInstance(baseInterface.getClass().getClassLoader(), proxyInterfaces, new HookHandler());
        } else {
            VLog.d(TAG, "Unable to build HookDelegate: %s.", getClass().getName());
        }
    }

    public HookDelegate(T baseInterface) {
        this(baseInterface, (Class[]) null);
    }

    /**
     * Copy all hooks from the input HookDelegate.
     *
     * @param from the HookDelegate we copy from.
     */
    public void copyHooks(HookDelegate from) {
        this.internalHookTable.putAll(from.getAllHooks());
    }

    /**
     * Add a Hook
     *
     * @param hook add a Hook
     */
    public Hook addHook(Hook hook) {
        if (hook != null && !TextUtils.isEmpty(hook.getName())) {
            if (internalHookTable.containsKey(hook.getName())) {
                VLog.w(TAG, "The Hook(%s, %s) you added has been in existence.", hook.getName(),
                        hook.getClass().getName());
                return hook;
            }
            internalHookTable.put(hook.getName(), hook);
        }
        return hook;
    }

    /**
     * Remove a startUniformer
     *
     * @param hookName The name of target Hook
     * @return The startUniformer you removed
     */
    public Hook removeHook(String hookName) {
        return internalHookTable.remove(hookName);
    }

    /**
     * Remove a startUniformer
     *
     * @param hook target Hook
     */
    public void removeHook(Hook hook) {
        if (hook != null) {
            removeHook(hook.getName());
        }
    }

    /**
     * 移除全部Hook
     */
    public void removeAllHook() {
        internalHookTable.clear();
    }

    /**
     * Get the startUniformer by its name.
     *
     * @param name name of the Hook
     * @param <H>  Type of the Hook
     * @return target startUniformer
     */
    @SuppressWarnings("unchecked")
    public <H extends Hook> H getHook(String name) {
        return (H) internalHookTable.get(name);
    }

    /**
     * @return Proxy interface
     */
    public T getProxyInterface() {
        return mProxyInterface;
    }

    /**
     * @return Origin Interface
     */
    public T getBaseInterface() {
        return mBaseInterface;
    }

    /**
     * @return count of the hooks
     */
    public int getHookCount() {
        return internalHookTable.size();
    }

    private class HookHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Hook hook = getHook(method.getName());
            try {
                if (hook != null && hook.isEnable()) {
                    if (hook.beforeCall(mBaseInterface, method, args)) {
                        Object res = hook.call(mBaseInterface, method, args);
                        res = hook.afterCall(mBaseInterface, method, args, res);
                        return res;
                    }
                }
                return method.invoke(mBaseInterface, args);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                if (cause != null) {
                    throw cause;
                }
                throw e;
            }
        }
    }

}
