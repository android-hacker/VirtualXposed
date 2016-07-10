package com.lody.virtual.client.hook.patchs.telephony_registry;

import java.lang.reflect.Method;

import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 *
 * @see com.android.internal.telephony.ITelephonyRegistry#addOnSubscriptionsChangedListener(String,
 *      IOnSubscriptionsChangedListener)
 */
/* package */ class Hook_AddOnSubscriptionsChangedListener extends Hook<TelephonyRegistryPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_AddOnSubscriptionsChangedListener(TelephonyRegistryPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "addOnSubscriptionsChangedListener";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
