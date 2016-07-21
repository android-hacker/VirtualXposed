package com.lody.virtual.client.hook.patchs.telephony_registry;

import com.android.internal.telephony.IOnSubscriptionsChangedListener;
import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.utils.HookUtils;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 *
 * @see com.android.internal.telephony.ITelephonyRegistry#addOnSubscriptionsChangedListener(String,
 *      IOnSubscriptionsChangedListener)
 */
/* package */ class Hook_RemoveOnSubscriptionsChangedListener extends Hook {

	@Override
	public String getName() {
		return "removeOnSubscriptionsChangedListener";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		HookUtils.replaceFirstAppPkg(args);
		return method.invoke(who, args);
	}
}
