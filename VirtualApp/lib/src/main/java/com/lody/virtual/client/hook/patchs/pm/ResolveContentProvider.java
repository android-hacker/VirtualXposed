package com.lody.virtual.client.hook.patchs.pm;

import android.content.pm.ProviderInfo;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.ipc.VPackageManager;
import com.lody.virtual.os.VUserHandle;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 */
/* package */ class ResolveContentProvider extends Hook {

	@Override
	public String getName() {
		return "resolveContentProvider";
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		String name = (String) args[0];
		int flags = (int) args[1];
		int userId = VUserHandle.myUserId();
		ProviderInfo info =  VPackageManager.get().resolveContentProvider(name, flags, userId);
		if (info == null) {
			if (name.equals("settings")) {
				info = (ProviderInfo) method.invoke(who, args);
			}
		}
		return info;
	}
}
