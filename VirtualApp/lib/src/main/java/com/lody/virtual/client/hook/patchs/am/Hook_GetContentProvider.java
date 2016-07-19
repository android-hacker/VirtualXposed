package com.lody.virtual.client.hook.patchs.am;

import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.IContentProvider;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.providers.ExternalProviderHook;
import com.lody.virtual.client.local.LocalContentManager;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author Lody
 *
 *
 * @see IActivityManager#getContentProvider(IApplicationThread, String, int,
 *      boolean) 原型: public ContentProviderHolder
 *      getContentProvider(IApplicationThread caller, String name)
 */
/* package */ class Hook_GetContentProvider extends Hook<ActivityManagerPatch> {

	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetContentProvider(ActivityManagerPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getContentProvider";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String name = (String) args[getProviderNameIndex()];
		IActivityManager.ContentProviderHolder holder = null;
		if (!VirtualCore.getCore().isHostProvider(name)) {
			holder = LocalContentManager.getDefault().getContentProvider(name);
		}
		if (holder == null) {
			holder = (IActivityManager.ContentProviderHolder) method.invoke(who, args);
		}
		ExternalProviderHook.HookFetcher fetcher = ExternalProviderHook.fetchHook(name);
		if (fetcher != null) {
			IContentProvider provider = holder.provider;
			ExternalProviderHook hook = fetcher.fetch(provider);
			holder.provider = (IContentProvider) Proxy.newProxyInstance(provider.getClass().getClassLoader(),
					new Class[]{IContentProvider.class}, hook);
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
