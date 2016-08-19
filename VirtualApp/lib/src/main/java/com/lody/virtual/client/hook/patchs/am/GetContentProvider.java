package com.lody.virtual.client.hook.patchs.am;

import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.IContentProvider;
import android.content.pm.ProviderInfo;
import android.os.IBinder;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.local.VActivityManager;
import com.lody.virtual.client.local.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see IActivityManager#getContentProvider(IApplicationThread, String, int,
 *      boolean)
 * @see IActivityManager#getContentProviderExternal(String, int, IBinder)
 *
 */
/* package */ class GetContentProvider extends Hook {
	@Override
	public String getName() {
		return "getContentProvider";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String name = (String) args[getProviderNameIndex()];
		int userId = VUserHandle.myUserId();
		ProviderInfo info = VPackageManager.get().resolveContentProvider(name, 0, userId);
		if (info != null) {
			if (info.processName.equals(VirtualRuntime.getProcessName())) {
				return new IActivityManager.ContentProviderHolder(info);
			}
			IContentProvider client = VActivityManager.get().acquireProviderClient(userId, info);
			IActivityManager.ContentProviderHolder holder = new IActivityManager.ContentProviderHolder(info);
			holder.provider = client;
			holder.noReleaseNeeded = true;
			return holder;
		} else {
			return method.invoke(who, args);
		}
	}

	public int getProviderNameIndex() {
		return 1;
	}

	@Override
	public boolean isEnable() {
		return isAppProcess();
	}
}
