package com.lody.virtual.client.hook.patchs.mount;

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
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
