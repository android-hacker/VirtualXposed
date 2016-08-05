package com.lody.virtual.client.hook.patchs.am;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import com.lody.virtual.client.env.RuntimeEnv;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.providers.ProviderHook;
import com.lody.virtual.client.local.LocalContentManager;
import com.lody.virtual.client.local.LocalPackageManager;
import com.lody.virtual.helper.utils.ComponentUtils;

import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.os.IBinder;

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
		if (willBlock(name)) {
			return null;
		}
		IActivityManager.ContentProviderHolder holder = LocalContentManager.getDefault().getContentProvider(name);
		if (holder == null) {
			try {
				holder = (IActivityManager.ContentProviderHolder) method.invoke(who, args);
				if (holder != null && holder.info != null && !ComponentUtils.isSystemApp(holder.info.applicationInfo)
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
		ProviderHook.HookFetcher fetcher = ProviderHook.fetchHook(name);
		if (fetcher != null) {
			IContentProvider provider = holder.provider;
			ProviderHook hook = fetcher.fetch(provider);
			holder.provider = (IContentProvider) Proxy.newProxyInstance(provider.getClass().getClassLoader(),
					new Class[]{IContentProvider.class}, hook);
		}
		return holder;
	}

	private boolean willBlock(String name) {
		ProviderInfo providerInfo = LocalPackageManager.getInstance().resolveContentProvider(name, 0);
		return providerInfo != null
				&& ComponentUtils.getProcessName(providerInfo).equals(RuntimeEnv.getCurrentProcessName());
	}

	public int getProviderNameIndex() {
		return 1;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
