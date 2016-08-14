package com.lody.virtual.client.hook.base;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;

import com.lody.virtual.client.interfaces.IHookObject;
import com.lody.virtual.helper.utils.VLog;

import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 *
 *
 *         掌握ServiceManager中的实权.代理所有接口.
 */
@SuppressWarnings("unchecked")
public abstract class HookBinder<Interface extends IInterface> implements IHookObject<Interface>, IBinder {

	private static final String TAG = HookBinder.class.getSimpleName();
	private static Map<String, IBinder> sCache;

	static {
		try {
			Class.forName(ServiceManager.class.getName());
			Field f_sCache = ServiceManager.class.getDeclaredField("sCache");
			f_sCache.setAccessible(true);
			sCache = (Map<String, IBinder>) f_sCache.get(null);
		} catch (Throwable e) {
			// 不考虑
		}
	}

	private IBinder baseBinder;
	private Interface mBaseObject;
	private Interface mProxyObject;
	/**
	 * 内部维护的Hook集合
	 */
	private Map<String, Hook> internalHookMapping = new HashMap<String, Hook>();

	public HookBinder() {
		bind();
	}

	public static Map<String, IBinder> getsCache() {
		return sCache;
	}

	protected abstract IBinder queryBaseBinder();

	protected abstract Interface createInterface(IBinder baseBinder);

	public final void bind() {
		this.baseBinder = queryBaseBinder();
		if (baseBinder != null) {
			this.mBaseObject = createInterface(baseBinder);
			mProxyObject = (Interface) Proxy.newProxyInstance(mBaseObject.getClass().getClassLoader(),
					mBaseObject.getClass().getInterfaces(), new HookHandler());
		}
	}

	@Override
	public String getInterfaceDescriptor() throws RemoteException {
		return baseBinder.getInterfaceDescriptor();
	}

	@Override
	public boolean pingBinder() {
		return baseBinder.pingBinder();
	}

	@Override
	public boolean isBinderAlive() {
		return baseBinder.isBinderAlive();
	}

	@Override
	public IInterface queryLocalInterface(String descriptor) {
		return getProxyObject();
	}

	@Override
	public void dump(FileDescriptor fd, String[] args) throws RemoteException {
		baseBinder.dump(fd, args);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	@Override
	public void dumpAsync(FileDescriptor fd, String[] args) throws RemoteException {
		baseBinder.dumpAsync(fd, args);
	}

	@Override
	public boolean transact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
		return baseBinder.transact(code, data, reply, flags);
	}

	@Override
	public void linkToDeath(DeathRecipient recipient, int flags) throws RemoteException {
		baseBinder.linkToDeath(recipient, flags);
	}

	@Override
	public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
		return baseBinder.unlinkToDeath(recipient, flags);
	}

	public IBinder getBaseBinder() {
		return baseBinder;
	}

	public void injectService(String name) throws Throwable {
		if (sCache != null) {
			sCache.remove(name);
			sCache.put(name, this);
		} else {
			throw new IllegalStateException("ServiceManager is invisible.");
		}
	}

	public void restoreService(String name) {
		if (sCache != null) {
			if (sCache.remove(name) != null) {
				sCache.put(name, baseBinder);
			}
		}
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
	@Override
	public <H extends Hook> H getHook(String name) {
		return (H) internalHookMapping.get(name);
	}

	/**
	 * @return 包装后的代理对象
	 */
	@Override
	public Interface getProxyObject() {
		return mProxyObject;
	}

	/**
	 * @return 原对象
	 */
	@Override
	public Interface getBaseObject() {
		return mBaseObject;
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
			VLog.w("XXXXXXXXXXXXXXX", "call %s (%s).", method.getName(), Arrays.toString(args));
			Hook hook = getHook(method.getName());
			try {
				if (hook != null && hook.isEnable()) {
					if (hook.beforeHook(mBaseObject, method, args)) {
						Object res = hook.onHook(mBaseObject, method, args);
						res = hook.afterHook(mBaseObject, method, args, res);
						return res;
					}
				}
				return method.invoke(mBaseObject, args);
			} catch (Throwable e) {
				if (e instanceof InvocationTargetException) {
					throw e.getCause();
				} else {
					throw e;
				}
			}
		}
	}
}
