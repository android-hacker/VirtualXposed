package com.lody.virtual.client.hook.patchs.mount;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
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

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		String path;
		if (args.length == 1) {
			path = (String) args[0];
		} else {
			path = (String) args[1];
		}
		try {
			return super.onHook(who, method, args);
		} catch (InvocationTargetException e) {
			if (!(e.getCause() instanceof SecurityException)) {
				throw e.getCause();
			}
		}
		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.exists() ? 0 : -1;
	}
}
