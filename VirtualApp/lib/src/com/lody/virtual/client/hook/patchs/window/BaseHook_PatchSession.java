package com.lody.virtual.client.hook.patchs.window;

import java.lang.reflect.Method;

import com.lody.virtual.client.hook.base.Hook;
import com.lody.virtual.client.hook.patchs.window.session.WindowSessionPatch;

import android.view.IWindowSession;

/**
 * @author Lody
 *
 */
/* package */ abstract class BaseHook_PatchSession extends Hook<WindowManagerPatch> {
	/**
	 * 这个构造器必须有,用于依赖注入.
	 *
	 * @param patchObject
	 *            注入对象
	 */
	public BaseHook_PatchSession(WindowManagerPatch patchObject) {
		super(patchObject);
	}

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
