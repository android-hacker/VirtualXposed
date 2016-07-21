package com.lody.virtual.client.hook.patchs.mount;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 * @see android.os.storage.IMountService#mkdirs(String, String)
 *
 */
/* package */ class Hook_Mkdirs extends Hook {


	@Override
	public String getName() {
		return "mkdirs";
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		HookUtils.replaceFirstAppPkg(args);
		return super.beforeHook(who, method, args);
	}
}
