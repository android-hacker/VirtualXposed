package com.lody.virtual.client.hook.patchs.telephony;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.hook.base.Hook;

import java.lang.reflect.Method;

/**
 * @author Lody
 * @see com.android.internal.telephony.ITelephony#getLine1NumberForDisplay(int,
 *      String)
 */

/* package */ class Hook_GetLine1NumberForDisplay extends Hook {

	@Override
	public String getName() {
		return "getLine1NumberForDisplay";
	}

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		// // FIXME: 16/7/2 运行度生活时,报错
		// Caused by: java.lang.IllegalArgumentException:
		// method
		// com.android.internal.telephony.ITelephony$Stub$Proxy.getLine1NumberForDisplay
		// argument 1 has type long, got java.lang.String
		if (args.length > 0 && (args[args.length - 1] instanceof String)) {
			if (isAppPkg(args[args.length - 1].toString())) {
				args[args.length - 1] = VirtualCore.getCore().getHostPkg();
			}
		}
		return method.invoke(who, args);
	}
}
