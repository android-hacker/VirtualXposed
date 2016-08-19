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
/* package */ class RemoveOnSubscriptionsChangedListener extends Hook {

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
