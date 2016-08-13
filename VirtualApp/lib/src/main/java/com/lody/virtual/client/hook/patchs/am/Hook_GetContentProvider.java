package com.lody.virtual.client.hook.patchs.am;

import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.os.IBinder;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.providers.ProviderHook;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.helper.utils.ComponentUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Lody
 *
 *
 * @see IActivityManager#getContentProvider(IApplicationThread, String, int,
 *      boolean)
 * @see IActivityManager#getContentProviderExternal(String, int, IBinder)
 *
 */
/* package */ class Hook_GetContentProvider extends Hook {

	@Override
	public String getName() {
		return "getContentProvider";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String name = (String) args[getProviderNameIndex()];
		ProviderInfo providerInfo = VPackageManager.getInstance().resolveContentProvider(name, 0);
		if (providerInfo != null) {
			if (getHostPkg().equals(providerInfo.packageName)) {
				return method.invoke(who, args);
			}
		}
		IActivityManager.ContentProviderHolder holder = VActivityManager.getInstance().getContentProvider(name);
		boolean external = holder == null;
		if (external) {
			try {
				holder = (IActivityManager.ContentProviderHolder) method.invoke(who, args);
				if (holder != null
						&& holder.info != null
						&& !ComponentUtils.isSystemApp(holder.info.applicationInfo)
						&& !getHostPkg().equals(holder.info.packageName)) {
					holder = null;
				}
			} catch (InvocationTargetException e) {
				if (e.getCause() instanceof SecurityException) {
					return null;
				}
				throw e.getCause();
			}
		}
		if (holder == null) {
			return null;
		}
		if (holder.provider == null) {
			return holder;
		}
		ProviderHook.HookFetcher fetcher = ProviderHook.fetchHook(name);
		if (fetcher != null) {
			IContentProvider provider = holder.provider;
			ProviderHook hook = fetcher.fetch(external, providerInfo, provider);
			if (hook != null) {
				holder.provider = (IContentProvider) Proxy.newProxyInstance(provider.getClass().getClassLoader(),
						new Class[]{IContentProvider.class}, hook);
			}
		}
		return holder;
	}

	public int getProviderNameIndex() {
		return 1;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
