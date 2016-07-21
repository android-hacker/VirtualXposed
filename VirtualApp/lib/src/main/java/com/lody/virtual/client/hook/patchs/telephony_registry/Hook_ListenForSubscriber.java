package com.lody.virtual.client.hook.patchs.telephony_registry;

import com.android.internal.telephony.IPhoneStateListener;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see com.android.internal.telephony.ITelephonyRegistry#listenForSubscriber(int,
 *      String, IPhoneStateListener, int, boolean)
 */
/* package */ class Hook_ListenForSubscriber extends Hook {

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
