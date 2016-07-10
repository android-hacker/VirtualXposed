package com.lody.virtual.client.hook.patchs.telephony;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 * @see com.android.internal.telephony.ITelephony#getCellLocation(String)
 *
 */
/* package */ class Hook_GetCellLocation extends Hook<TelephonyPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetCellLocation(TelephonyPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getCellLocation";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceLastAppPkg(args);
		return method.invoke(who, args);
	}
}
