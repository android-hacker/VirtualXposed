package com.lody.virtual.client.hook.base;

import android.text.TextUtils;

import com.lody.virtual.client.hook.utils.HookUtils;
import com.lody.virtual.client.interfaces.IHookObject;
import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 *
 */
@SuppressWarnings("unchecked")
public abstract class HookDelegate<T> implements IHookObject {

	private static final String TAG = HookDelegate.class.getSimpleName();
	private T mBaseInterface;
	private T mProxyInterface;
	/**
	 * 内部维护的Hook集合
	 */
	private Map<String, Hook> internalHookMapping = new HashMap<String, Hook>();

	@Override
	public Map<String, Hook> getAllHooks() {
		return internalHookMapping;
	}


	public HookDelegate(Class<?>... proxyInterfaces) {
		mBaseInterface = createInterface();
		if (mBaseInterface != null) {
			if (proxyInterfaces == null) {
				proxyInterfaces = HookUtils.getAllInterface(mBaseInterface.getClass());
			}
			mProxyInterface = (T) Proxy.newProxyInstance(mBaseInterface.getClass().getClassLoader(), proxyInterfaces, new HookHandler());
		} else {
			VLog.d(TAG, "Unable to build HookDelegate: %s.", getClass().getName());
		}
	}

	public HookDelegate() {
		this((Class[]) null);
	}


	protected abstract T createInterface();

	@Override
	public void copyHooks(IHookObject from) {
		this.internalHookMapping.putAll(from.getAllHooks());
	}

	/**
	 * 添加一个Hook
	 * 
	 * @param hook
	 *            要添加的Hook
	 */
	@Override
	public Hook addHook(Hook hook) {
		if (hook != null && !TextUtils.isEmpty(hook.getName())) {
			if (internalHookMapping.containsKey(hook.getName())) {
				VLog.w(TAG, "Hook(%s) from class(%s) have been added, can't add again.", hook.getName(),
						hook.getClass().getName());
				return hook;
			}
			internalHookMapping.put(hook.getName(), hook);
		}
		return hook;
	}

	/**
	 * 移除一个Hook
	 * 
	 * @param hookName
	 *            要移除的Hook名
	 * @return 移除的Hook
	 */
	@Override
	public Hook removeHook(String hookName) {
		return internalHookMapping.remove(hookName);
	}

	/**
	 * 移除一个Hook
	 * 
	 * @param hook
	 *            要移除的Hook
	 */
	@Override
	public void removeHook(Hook hook) {
		if (hook != null) {
			removeHook(hook.getName());
		}
	}

	/**
	 * 移除全部Hook
	 */
	@Override
	public void removeAllHook() {
		internalHookMapping.clear();
	}

	/**
	 * 取得指定名称的Hook
	 *
	 * @param name
	 *            Hook名
	 * @param <H>
	 *            Hook类型
	 * @return 指定名称的Hook
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <H extends Hook> H getHook(String name) {
		return (H) internalHookMapping.get(name);
	}

	/**
	 * @return 包装后的代理对象
	 */
	@Override
	public T getProxyInterface() {
		return mProxyInterface;
	}

	/**
	 * @return 原对象
	 */
	@Override
	public T getBaseInterface() {
		return mBaseInterface;
	}

	/**
	 * @return Hook数量
	 */
	@Override
	public int getHookCount() {
		return internalHookMapping.size();
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
