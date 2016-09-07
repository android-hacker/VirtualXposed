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
/* package */ class GetVolumeList extends Hook {

	@Override
	public String getName() {
		return "getVolumeList";
	}

	@Override
	public boolean beforeCall(Object who, Method method, Object... args) {
		HookUtils.replaceFirstAppPkg(args);
		return super.beforeCall(who, method, args);
	}

	@Override
	public Object afterCall(Object who, Method method, Object[] args, Object result) throws Throwable {
		return result;
	}
}
