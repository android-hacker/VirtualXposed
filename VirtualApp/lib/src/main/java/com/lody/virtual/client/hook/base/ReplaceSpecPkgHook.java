package com.lody.virtual.client.hook.base;

import java.lang.reflect.Method;

/**
 * @author Lody
 */

public class ReplaceSpecPkgHook extends StaticHook {

	private int index;

	public ReplaceSpecPkgHook(String name, int index) {
		super(name);
		this.index = index;
	}

	@Override
	public boolean beforeCall(Object who, Method method, Object... args) {
		if (args != null) {
			int i = index;
			if (i < 0) {
				i += args.length;
			}
			if (i >= 0 && i < args.length && args[i] instanceof String) {
				args[i] = getHostPkg();
			}
		}
		return super.beforeCall(who, method, args);
	}
}
