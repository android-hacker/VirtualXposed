package com.lody.virtual.client.hook.providers;

import android.net.Uri;
import android.os.Bundle;
import android.os.IInterface;

import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import mirror.android.content.IContentProvider;

/**
 * @author Lody
 *
 *
 */

public class ProviderHook implements InvocationHandler {

	private static final Map<String, HookFetcher> PROVIDER_MAP = new HashMap<>();

	static {
		PROVIDER_MAP.put("settings", new HookFetcher() {
			@Override
			public ProviderHook fetch(boolean external, IInterface provider) {
				return new SettingsProviderHook(provider);
			}
		});
		PROVIDER_MAP.put("downloads", new HookFetcher() {
			@Override
			public ProviderHook fetch(boolean external, IInterface provider) {
				return new DownloadProviderHook(provider);
			}
		});
	}

	protected final Object mBase;

	public ProviderHook(Object base) {
		this.mBase = base;
	}

	private static HookFetcher fetchHook(String authority) {
		HookFetcher fetcher = PROVIDER_MAP.get(authority);
		if (fetcher == null) {
			fetcher = new HookFetcher() {
				@Override
				public ProviderHook fetch(boolean external, IInterface provider) {
					if (external) {
						return new ExternalProviderHook(provider);
					}
					return new InternalProviderHook(provider);
				}
			};
		}
		return fetcher;
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
			VLog.d("ProviderHook", "call: %s (%s) with error", method.getName(), Arrays.toString(args));
			if (e instanceof InvocationTargetException) {
				throw e.getCause();
			}
			throw e;
		}
	}

	protected void processArgs(Method method, Object... args) {

	}

	private static IInterface createProxy(IInterface provider, ProviderHook hook) {
		if (provider == null || hook == null) {
			return null;
		}
		return (IInterface) Proxy.newProxyInstance(provider.getClass().getClassLoader(), new Class[] {
				IContentProvider.TYPE,
		}, hook);
	}

	public static IInterface createProxy(boolean external, String authority, IInterface provider) {
		if (provider instanceof Proxy && Proxy.getInvocationHandler(provider) instanceof ProviderHook) {
			return provider;
		}
		ProviderHook.HookFetcher fetcher = ProviderHook.fetchHook(authority);
		if (fetcher != null) {
			ProviderHook hook = fetcher.fetch(external, provider);
			IInterface proxyProvider = ProviderHook.createProxy(provider, hook);
			if (proxyProvider != null) {
				provider = proxyProvider;
			}
		}
		return provider;
	}

	public interface HookFetcher {
		ProviderHook fetch(boolean external, IInterface provider);
	}
}
