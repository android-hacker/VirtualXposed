package com.lody.virtual.client.hook.patchs.window;


import android.os.IInterface;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.patchs.window.session.WindowSessionPatch;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/*package*/ abstract class BasePatchSession extends Hook {

	@Override
	public Object call(Object who, Method method, Object... args) throws Throwable {
		Object session = method.invoke(who, args);
		if (session instanceof IInterface) {
			return patchSession((IInterface) session);
		}
		return session;
	}

	private Object patchSession(IInterface session) {
		WindowSessionPatch windowSessionPatch = new WindowSessionPatch(session);
		return windowSessionPatch.getHookDelegate().getProxyInterface();
	}
}
