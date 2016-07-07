package com.lody.virtual.client.hook.patchs.restriction;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 */

/* package */ class Hook_GetApplicationRestrictions extends Hook<RestrictionPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetApplicationRestrictions(RestrictionPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getApplicationRestrictions";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
