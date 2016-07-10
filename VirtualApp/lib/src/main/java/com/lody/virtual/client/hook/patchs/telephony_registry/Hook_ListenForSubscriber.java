package com.lody.virtual.client.hook.patchs.telephony_registry;

import java.lang.reflect.Method;

import com.android.internal.telephony.IPhoneStateListener;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

/**
 * @author Lody
 *
 *
 * @see com.android.internal.telephony.ITelephonyRegistry#listenForSubscriber(int,
 *      String, IPhoneStateListener, int, boolean)
 */
/* package */ class Hook_ListenForSubscriber extends Hook<TelephonyRegistryPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public Hook_ListenForSubscriber(TelephonyRegistryPatch patchObject) {
		super(patchObject);
	}

	@Override
	public String getName() {
		return "listenForSubscriber";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}

}
