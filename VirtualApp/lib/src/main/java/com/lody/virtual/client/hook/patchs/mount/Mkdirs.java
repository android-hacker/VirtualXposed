package com.lody.virtual.client.hook.patchs.mount;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 */
/* package */ class Mkdirs extends Hook {

	@Override
	public String getName() {
		return "mkdirs";
	}

	@Override
	public boolean beforeCall(Object who, Method method, Object... args) {
		HookUtils.replaceFirstAppPkg(args);
		return super.beforeCall(who, method, args);
	}

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		try {
			return super.call(who, method, args);
		} catch (InvocationTargetException e) {
			if (!(e.getCause() instanceof SecurityException)) {
				throw e.getCause();
			}
		}
		String path;
		if (args.length == 1) {
			path = (String) args[0];
		} else {
			path = (String) args[1];
		}
		File file = new File(path);
		return file.exists() && file.mkdirs() ? 0 : -1;
	}
}
