package com.lody.virtual.client.hook.providers;

import android.content.IContentProvider;
import android.net.Uri;
import android.os.Bundle;

import com.lody.virtual.client.core.AppSandBox;
import com.lody.virtual.client.core.VirtualCore;

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

public class ExternalProviderHook implements InvocationHandler {

	private static final Map<String, HookFetcher> PROVIDER_MAP = new HashMap<>();

	public interface HookFetcher {
		ExternalProviderHook fetch(IContentProvider provider);
	}

	public static HookFetcher fetchHook(String authority) {
		return PROVIDER_MAP.get(authority);
	}

	static {
		PROVIDER_MAP.put("settings", new HookFetcher() {
			@Override
			public ExternalProviderHook fetch(IContentProvider provider) {
				return new SettingsProviderHook(provider);
			}
		});
	}

	private Object mBase;

	public ExternalProviderHook(Object base) {
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
		if (args != null && args.length > 0 && args[0] instanceof String) {
			String pkg = (String) args[0];
			if (VirtualCore.getCore().getHostPkg().equals(pkg)) {
				String lastPkg = AppSandBox.getLastPkg();
				if (lastPkg != null) {
					args[0] = lastPkg;
				}
			}
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
}
