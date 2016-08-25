package com.lody.virtual.client.hook.patchs.display;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 */
/* package */ class CreateVirtualDisplay extends Hook {

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		HookUtils.replaceFirstAppPkg(args);
		return super.beforeHook(who, method, args);
	}

	@Override
	public String getName() {
		return "createVirtualDisplay";
	}

}
