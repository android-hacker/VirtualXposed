package com.lody.virtual.client.hook.patchs.window;

import com.lody.virtual.client.hook.base.Patch;
import com.lody.virtual.client.hook.base.PatchObject;
import com.lody.virtual.client.hook.binders.HookWindowManagerBinder;
import com.lody.virtual.helper.utils.Reflect;

import android.content.Context;
import android.os.ServiceManager;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;

/**
 * @author Lody
 *
 *
 * @see IWindowManager
 */
@Patch({Hook_OverridePendingAppTransition.class, Hook_OverridePendingAppTransitionInPlace.class, Hook_OpenSession.class,
		Hook_SetAppStartingWindow.class})
public class WindowManagerPatch extends PatchObject<IWindowManager, HookWindowManagerBinder> {

	@Override
	protected HookWindowManagerBinder initHookObject() {
		return new HookWindowManagerBinder();
	}

	@Override
	public void inject() throws Throwable {

		getHookObject().injectService(Context.WINDOW_SERVICE);
		IWindowManager hookedWM = getHookObject().getProxyObject();
		try {
			Reflect.on(WindowManagerGlobal.class).set("sWindowManagerService", hookedWM);
		} catch (Throwable e) {
			// Ignore
		}

		Class<?> phoneWindowHolderClass = null;
		try {
			phoneWindowHolderClass = Class.forName("com.android.internal.policy.impl.PhoneWindow$WindowManagerHolder");
		} catch (Throwable e) {
			// Ignore
		}
		if (phoneWindowHolderClass == null) {
			try {
				phoneWindowHolderClass = Class
						.forName("com.android.internal.policy.impl.PhoneWindow$WindowManagerHolder");
			} catch (Throwable e) {
				// Ignore
			}
		}
		if (phoneWindowHolderClass != null) {
			try {
				Reflect.on(phoneWindowHolderClass).set("sWindowManager", hookedWM);
			} catch (Throwable e) {
				// Ignore
			}
		}
	}

	@Override
	public boolean isEnvBad() {
		return ServiceManager.getService(Context.WINDOW_SERVICE) != getHookObject();
	}
}
