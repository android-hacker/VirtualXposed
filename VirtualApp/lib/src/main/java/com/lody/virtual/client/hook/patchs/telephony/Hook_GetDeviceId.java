package com.lody.virtual.client.hook.patchs.telephony;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 * @see com.android.internal.telephony.ITelephony#getDeviceId(String)
 *
 */
/* package */ class Hook_GetDeviceId extends Hook<TelephonyPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_GetDeviceId(TelephonyPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "getDeviceId";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}

}
