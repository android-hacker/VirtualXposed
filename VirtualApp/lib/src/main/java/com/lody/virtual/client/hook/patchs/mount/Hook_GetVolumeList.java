package com.lody.virtual.client.hook.patchs.mount;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 *         原型: public StorageVolume[] getVolumeList(int uid, String packageName,
 *         int flags)
 */
/* package */ class Hook_GetVolumeList extends Hook {

	@Override
	public String getName() {
		return "getVolumeList";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		if (args.length >= 1) {
			if (args[0] instanceof Integer) {
				args[0] = VirtualCore.getCore().myUid();
			}
		}
		HookUtils.replaceFirstAppPkg(args);
		return super.beforeHook(who, method, args);
	}
}
