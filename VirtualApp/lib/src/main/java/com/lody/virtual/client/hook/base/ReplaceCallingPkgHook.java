package com.lody.virtual.client.hook.base;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 */

public class ReplaceCallingPkgHook extends StaticHook {

	public ReplaceCallingPkgHook(String name) {
		super(name);
	}

	@Override
	public boolean beforeHook(Object who, Method method, Object... args) {
		HookUtils.replaceFirstAppPkg(args);
		return super.beforeHook(who, method, args);
	}
}
