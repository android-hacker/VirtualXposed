package com.lody.virtual.client.hook.patchs.pm;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ class Hook_AddPackageToPreferred extends Hook {

	@Override
	public String getName() {
		return "addPackageToPreferred";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		HookUtils.replaceFirstAppPkg(args);
		return true;
	}
}
