package com.lody.virtual.client.hook.patchs.am;

import android.content.pm.ApplicationInfo;
import android.content.pm.ProviderInfo;
import android.os.IInterface;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.providers.ProviderHook;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

import mirror.android.app.IActivityManager;

/**
 * @author Lody
 */
/* package */ class GetContentProvider extends Hook {
	@Override
	public String getName() {
		return "getContentProvider";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String name = (String) args[getProviderNameIndex()];
		int userId = VUserHandle.myUserId();
		ProviderInfo info = VPackageManager.get().resolveContentProvider(name, 0, userId);
		if (info != null) {
			if (info.processName.equals(VirtualRuntime.getProcessName())) {
				return IActivityManager.ContentProviderHolder.ctor.newInstance(info);
			}
			IInterface client = VActivityManager.get().acquireProviderClient(userId, info);
			Object holder = IActivityManager.ContentProviderHolder.ctor.newInstance(info);
			IActivityManager.ContentProviderHolder.info.set(holder, info);
			IActivityManager.ContentProviderHolder.provider.set(holder, client);
			IActivityManager.ContentProviderHolder.noReleaseNeeded.set(holder, true);
			return holder;
		} else {
			Object holder = method.invoke(who, args);
			if (holder != null) {
				info = IActivityManager.ContentProviderHolder.info.get(holder);
				if (!shouldVisible(info)) {
					return null;
				}
				IInterface provider = IActivityManager.ContentProviderHolder.provider.get(holder);
				if (!ProviderHook.class.isInstance(provider)) {
					ProviderHook.HookFetcher fetcher = ProviderHook.fetchHook(info.authority);
					if (fetcher != null) {
						ProviderHook hook = fetcher.fetch(true, info, provider);
						if (hook != null) {
							IInterface proxyProvider = ProviderHook.createProxy(provider, hook);
							if (proxyProvider != null) {
								IActivityManager.ContentProviderHolder.provider.set(holder, proxyProvider);
							}
						}
					}
				}
			}
			return holder;
		}
	}

	private boolean shouldVisible(ProviderInfo info) {
		return info.packageName.equals(getHostPkg())
				|| (info.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
	}

	public int getProviderNameIndex() {
		return 1;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
