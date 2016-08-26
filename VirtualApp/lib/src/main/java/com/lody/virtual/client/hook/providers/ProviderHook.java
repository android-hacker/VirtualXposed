package com.lody.virtual.client.hook.providers;

import android.content.pm.ProviderInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.IInterface;

import com.lody.virtual.helper.utils.VLog;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
			public ProviderHook fetch(boolean external, ProviderInfo info, IInterface provider) {
				return new SettingsProviderHook(provider);
			}
		});
		PROVIDER_MAP.put("downloads", new HookFetcher() {
			@Override
			public ProviderHook fetch(boolean external, ProviderInfo info, IInterface provider) {
				return new DownloadProviderHook(provider);
			}
		});
	}

	protected final Object mBase;

	public ProviderHook(Object base) {
		this.mBase = base;
	}

	public static HookFetcher fetchHook(String authority) {
		HookFetcher fetcher = PROVIDER_MAP.get(authority);
		if (fetcher == null) {
			fetcher = new HookFetcher() {
				@Override
				public ProviderHook fetch(boolean external, ProviderInfo info, IInterface provider) {
					if (external) {
						return new ExternalProviderHook(provider);
					}
					return null;
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
			VLog.d("###########", "call: %s (%s) with error", method.getName(), Arrays.toString(args));
			if (e instanceof InvocationTargetException) {
				throw e.getCause();
			}
			throw e;
		}
	}

	protected void processArgs(Method method, Object... args) {

	}

	public interface HookFetcher {
		ProviderHook fetch(boolean external, ProviderInfo info, IInterface provider);
	}
}
