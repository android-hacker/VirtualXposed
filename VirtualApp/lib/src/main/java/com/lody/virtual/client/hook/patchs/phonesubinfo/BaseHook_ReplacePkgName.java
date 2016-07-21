package com.lody.virtual.client.hook.patchs.phonesubinfo;

import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ abstract class BaseHook_ReplacePkgName extends Hook {

	protected abstract int getIndex();

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		if (args != null) {
			int index = getIndex();
			if (index == -2) {
				index = args.length - 1;
			}
			if (index < args.length && args[index] instanceof String) {
				String pkgName = (String) args[index];
				if (isAppPkg(pkgName)) {
					args[index] = getHostPkg();
				}
			}
		}
		return method.invoke(who, args);
	}
}
