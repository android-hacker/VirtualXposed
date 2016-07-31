package com.lody.virtual.client.hook.providers;

import android.content.IContentProvider;
import android.net.Uri;
import android.os.Bundle;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Lody
 *
 * @see android.content.IContentProvider
 *
 */

public class ProviderHook implements InvocationHandler {

	private static final Map<String, HookFetcher> PROVIDER_MAP = new HashMap<>();

	public interface HookFetcher {
		ProviderHook fetch(IContentProvider provider);
	}

	public static HookFetcher fetchHook(String authority) {
		return PROVIDER_MAP.get(authority);
	}

	static {
		PROVIDER_MAP.put("settings", new HookFetcher() {
			@Override
			public ProviderHook fetch(IContentProvider provider) {
				return new SettingsProviderHook(provider);
			}
		});
	}

	protected final Object mBase;

	public ProviderHook(Object base) {
		this.mBase = base;
	}

	public Bundle call(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		return (Bundle) method.invoke(mBase, args);
	}

	public Uri insert(Method method, Object[] args) throws InvocationTargetException, IllegalAccessException {
		return (Uri) method.invoke(mBase, args);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		try {
			processArgs(method, args);
		} catch (Throwable e) {
			e.printStackTrace();
		}
		try {
			String name = method.getName();
			if ("call".equals(name)) {
				return call(method, args);
			} else if ("insert".equals(name)) {
				return insert(method, args);
			}
			return method.invoke(mBase, args);
		} catch (Throwable e) {
			if (e instanceof InvocationTargetException) {
				throw e.getCause();
			}
			throw e;
		}
	}

	protected void processArgs(Method method, Object... args) {

	}
}
