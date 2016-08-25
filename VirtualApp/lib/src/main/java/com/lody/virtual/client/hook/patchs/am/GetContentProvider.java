package com.lody.virtual.client.hook.patchs.am;

import android.content.pm.ProviderInfo;
import android.os.IInterface;

import com.lody.virtual.client.env.VirtualRuntime;
import com.lody.virtual.client.hook.base.Hook;
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
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String name = (String) args[getProviderNameIndex()];
		int userId = VUserHandle.myUserId();
		ProviderInfo info = VPackageManager.get().resolveContentProvider(name, 0, userId);
		if (info != null) {
			if (info.processName.equals(VirtualRuntime.getProcessName())) {
				return IActivityManager.ContentProviderHolder.ctor.newInstance(info);
			}
			IInterface client = VActivityManager.get().acquireProviderClient(userId, info);
			Object holder = IActivityManager.ContentProviderHolder.ctor.newInstance(info);
			IActivityManager.ContentProviderHolder.provider.set(holder, client);
			IActivityManager.ContentProviderHolder.noReleaseNeeded.set(holder, true);
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
