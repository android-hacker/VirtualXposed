package com.lody.virtual.client.hook.patchs.window;

import android.view.IWindowSession;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.patchs.window.session.WindowSessionPatch;

import java.lang.reflect.Method;

/**
 * @author Lody
 *
 */
/* package */ abstract class BaseHook_PatchSession extends Hook {

	@Override
	public Object onHook(Object who, Method method, Object... args) throws Throwable {
		Object session = method.invoke(who, args);
		if (session instanceof IWindowSession) {
			return patchSession((IWindowSession) session);
		}
		return session;
	}

	private Object patchSession(IWindowSession session) {
		WindowSessionPatch windowSessionPatch = new WindowSessionPatch(session);
		return windowSessionPatch.getHookObject().getProxyObject();
	}
}
